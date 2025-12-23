package com.example.eSign.service.impl;

import com.example.eSign.dto.PdfSignBase64Request;
import com.example.eSign.dto.PdfSignatureVerificationInfo;
import com.example.eSign.dto.PdfVerificationResult;
import com.example.eSign.service.PdfVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.util.Store;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfVerificationServiceImpl implements PdfVerificationService {

    private final Certificate certificate;

    // All dates in responses should be in IST with pattern yyyy-MM-dd HH:mm:ss
    private static final DateTimeFormatter RESPONSE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Override
    public PdfVerificationResult verifyPdfFromBase64(PdfSignBase64Request request) throws IOException {
        if (request == null || request.getPdfBase64() == null || request.getPdfBase64().isEmpty()) {
            throw new IllegalArgumentException("pdfBase64 is required");
        }

        byte[] pdfBytes;
        try {
            pdfBytes = Base64.getDecoder().decode(request.getPdfBase64());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 PDF content", e);
        }

        return verifyPdf(pdfBytes);
    }

    /**
     * Verifies all signatures on the given PDF bytes.
     */
    private PdfVerificationResult verifyPdf(byte[] pdfBytes) throws IOException {
        log.debug("Starting PDF verification");

        List<PdfSignatureVerificationInfo> verificationInfos = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            List<PDSignature> pdSignatures = document.getSignatureDictionaries();

            if (pdSignatures == null || pdSignatures.isEmpty()) {
                return new PdfVerificationResult(
                        false,
                        0,
                        "Document does not contain any digital signatures",
                        Collections.emptyList()
                );
            }

            boolean allValid = true;

            for (PDSignature sig : pdSignatures) {
                PdfSignatureVerificationInfo info = verifySingleSignature(sig, pdfBytes);
                verificationInfos.add(info);
                if (!info.isIntegrityValid() || !info.isCertificateValid() || !info.isTrusted()) {
                    allValid = false;
                }
            }

            String message = allValid
                    ? "All signatures are valid and trusted"
                    : "One or more signatures failed verification or are not trusted";

            return new PdfVerificationResult(
                    allValid,
                    verificationInfos.size(),
                    message,
                    verificationInfos
            );
        } catch (IOException e) {
            log.error("IO error while verifying PDF signatures", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while verifying PDF signatures", e);
            throw new IOException("Unexpected error while verifying PDF signatures: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies a single signature, splitting work into integrity and certificate checks.
     */
    private PdfSignatureVerificationInfo verifySingleSignature(PDSignature signature, byte[] pdfBytes) {
        String signingTime = null;
        if (signature.getSignDate() != null) {
            signingTime = signature.getSignDate()
                    .toInstant()
                    .atZone(IST_ZONE)
                    .format(RESPONSE_DATE_FORMATTER);
        }

        boolean integrityValid = false;
        boolean certificateValid = false;
        boolean trusted = false;
        String signerName = signature.getName();
        String message;

        try {
            byte[] signedContent = extractSignedContent(signature, pdfBytes);
            CMSSignedData cmsSignedData = new CMSSignedData(new CMSProcessableByteArray(signedContent),
                    signature.getContents());

            // Integrity check – verify CMS signature over the signed content.
            integrityValid = verifySignatureIntegrity(cmsSignedData);

            // Certificate extraction and validation.
            X509Certificate signerCert = extractSignerCertificate(cmsSignedData);
            if (signerCert != null) {
                signerName = extractCommonName(signerCert.getSubjectX500Principal().getName());
                certificateValid = validateCertificateDates(signerCert);
                trusted = isTrustedSigner(signerCert);
            }

            if (integrityValid && certificateValid && trusted) {
                message = "Signature is cryptographically valid and trusted";
            } else if (!integrityValid) {
                message = "Signature integrity check failed";
            } else if (!certificateValid) {
                message = "Signer certificate is not within its validity period";
            } else {
                message = "Signer certificate is not trusted by server configuration";
            }
        } catch (Exception e) {
            log.error("Failed to verify individual PDF signature", e);
            message = "Error while verifying signature: " + e.getMessage();
        }

        return new PdfSignatureVerificationInfo(
                signerName,
                signingTime,
                integrityValid,
                certificateValid,
                trusted,
                message
        );
    }

    /**
     * Extracts the signed content bytes based on the PDF signature's byte range.
     */
    private byte[] extractSignedContent(PDSignature signature, byte[] pdfBytes) throws IOException {
        int[] byteRange = signature.getByteRange();
        if (byteRange == null || byteRange.length != 4) {
            throw new IOException("Invalid signature byte range");
        }

        int fileSize = pdfBytes.length;
        for (int i = 0; i < byteRange.length; i += 2) {
            int start = byteRange[i];
            int length = byteRange[i + 1];
            if (start < 0 || length < 0 || start + length > fileSize) {
                throw new IOException("Signature byte range is outside of PDF content");
            }
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int i = 0; i < byteRange.length; i += 2) {
            int start = byteRange[i];
            int length = byteRange[i + 1];
            output.write(pdfBytes, start, length);
        }
        return output.toByteArray();
    }

    /**
     * Verifies CMS signature integrity (cryptographic signature over the signed bytes).
     */
    private boolean verifySignatureIntegrity(CMSSignedData cmsSignedData) throws Exception {
        SignerInformationStore signerInfos = cmsSignedData.getSignerInfos();
        Iterator<SignerInformation> it = signerInfos.getSigners().iterator();
        if (!it.hasNext()) {
            throw new CMSException("No signer information present");
        }

        SignerInformation signerInformation = it.next();

        Store<X509CertificateHolder> certificatesStore = cmsSignedData.getCertificates();
        @SuppressWarnings("unchecked")
        Iterator<X509CertificateHolder> certIt =
                certificatesStore.getMatches(signerInformation.getSID()).iterator();
        if (!certIt.hasNext()) {
            throw new CMSException("No matching certificate for signer");
        }

        X509CertificateHolder certHolder = certIt.next();
        X509Certificate signerCert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        return signerInformation.verify(
                new JcaSimpleSignerInfoVerifierBuilder()
                        .setProvider("BC")
                        .build(signerCert)
        );
    }

    /**
     * Extracts the signer's certificate from CMS signed data.
     */
    private X509Certificate extractSignerCertificate(CMSSignedData cmsSignedData) throws Exception {
        SignerInformationStore signerInfos = cmsSignedData.getSignerInfos();
        Iterator<SignerInformation> it = signerInfos.getSigners().iterator();
        if (!it.hasNext()) {
            return null;
        }

        SignerInformation signerInformation = it.next();

        Store<X509CertificateHolder> certificatesStore = cmsSignedData.getCertificates();
        @SuppressWarnings("unchecked")
        Iterator<X509CertificateHolder> certIt =
                certificatesStore.getMatches(signerInformation.getSID()).iterator();
        if (!certIt.hasNext()) {
            return null;
        }

        X509CertificateHolder certHolder = certIt.next();
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);
    }

    /**
     * Validates certificate validity period (notBefore / notAfter).
     */
    private boolean validateCertificateDates(X509Certificate signerCert) {
        try {
            signerCert.checkValidity();
            return true;
        } catch (Exception e) {
            log.warn("Certificate validity check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Determines whether the signer certificate is trusted.
     * <p>
     * For now, this implementation considers a certificate trusted if it matches
     * the certificate configured for signing in the keystore. This can be
     * extended later to perform full chain/OCSP/CRL validation.
     */
    private boolean isTrustedSigner(X509Certificate signerCert) {
        if (!(certificate instanceof X509Certificate configuredCert)) {
            return false;
        }

        return configuredCert.getSerialNumber().equals(signerCert.getSerialNumber())
                && configuredCert.getSubjectX500Principal().equals(signerCert.getSubjectX500Principal());
    }

    /**
     * Extracts the Common Name (CN=...) from a DN string if present,
     * otherwise returns the original DN.
     */
    private String extractCommonName(String dn) {
        if (dn == null || dn.isBlank()) {
            return dn;
        }
        String[] parts = dn.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("CN=") && trimmed.length() > 3) {
                return trimmed.substring(3);
            }
        }
        return dn;
    }
}


