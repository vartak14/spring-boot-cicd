package com.example.eSign.service.impl;

import com.example.eSign.dto.PdfSignBase64Request;
import com.example.eSign.dto.PdfSignResponse;
import com.example.eSign.dto.PdfSignatureInfo;
import com.example.eSign.dto.PdfSignatureListResponse;
import com.example.eSign.dto.SignerInfoResponse;
import com.example.eSign.service.PdfSigningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfSigningServiceImpl implements PdfSigningService {

    private final PrivateKey privateKey;
    private final Certificate certificate;

    @Value("${app.keystore.alias}")
    private String defaultAlias;

    // All dates in responses should be in IST with pattern yyyy-MM-dd HH:mm:ss
    private static final DateTimeFormatter RESPONSE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Override
    public byte[] signPdf(byte[] pdfBytes) throws IOException {
        log.debug("Starting PDF signing operation");

        try (PDDocument document = Loader.loadPDF(pdfBytes);
             ByteArrayOutputStream signedOut = new ByteArrayOutputStream()) {

            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("eSign");
            signature.setLocation("eSign Service");
            signature.setReason("Document signed electronically");
            signature.setSignDate(Calendar.getInstance());

            SignatureOptions options = new SignatureOptions();
            options.setPreferredSignatureSize(8192);

            document.addSignature(signature, (SignatureInterface) content -> {
                try {
                    ByteArrayInputStream contentStream = new ByteArrayInputStream(content.readAllBytes());
                    byte[] cmsSignature = createCmsSignature(contentStream);
                    return cmsSignature;
                } catch (Exception e) {
                    log.error("Error while creating CMS signature", e);
                    throw new IOException("Failed to create CMS signature: " + e.getMessage(), e);
                }
            }, options);

            document.saveIncremental(signedOut);
            log.info("PDF signed successfully");
            return signedOut.toByteArray();
        } catch (IOException e) {
            log.error("IO error while signing PDF", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while signing PDF", e);
            throw new IOException("Unexpected error while signing PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PdfSignatureInfo> extractSignatures(byte[] pdfBytes) throws IOException {
        log.debug("Starting PDF signature extraction");

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            List<PDSignature> pdSignatures = document.getSignatureDictionaries();
            List<PdfSignatureInfo> result = new ArrayList<>();

            // Derive signer name from the certificate we use for signing,
            // so API always returns the real signer identity, not just
            // the free-text PDSignature name field.
            String signerNameFromCertificate = null;
            if (certificate instanceof X509Certificate x509) {
                String subjectDn = x509.getSubjectX500Principal().getName();
                signerNameFromCertificate = extractCommonName(subjectDn);
            }

            for (PDSignature sig : pdSignatures) {
                String isoDate = null;
                if (sig.getSignDate() != null) {
                    isoDate = sig.getSignDate()
                            .toInstant()
                            .atZone(IST_ZONE)
                            .format(RESPONSE_DATE_FORMATTER);
                }

                PdfSignatureInfo info = new PdfSignatureInfo(
                        signerNameFromCertificate != null ? signerNameFromCertificate : sig.getName(),
                        sig.getLocation(),
                        sig.getReason(),
                        isoDate,
                        sig.getSubFilter()
                );
                result.add(info);
            }

            log.info("Found {} signatures on PDF", result.size());
            return result;
        } catch (IOException e) {
            log.error("IO error while extracting signatures from PDF", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while extracting signatures from PDF", e);
            throw new IOException("Unexpected error while extracting signatures from PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public PdfSignResponse signPdfFromBase64(PdfSignBase64Request request) throws IOException {
        if (request == null || request.getPdfBase64() == null || request.getPdfBase64().isEmpty()) {
            throw new IllegalArgumentException("pdfBase64 is required");
        }

        byte[] pdfBytes;
        try {
            pdfBytes = Base64.getDecoder().decode(request.getPdfBase64());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 PDF content", e);
        }

        byte[] signedBytes = signPdf(pdfBytes);
        String signedBase64 = Base64.getEncoder().encodeToString(signedBytes);
        String fileName = (request.getFileName() != null && !request.getFileName().isEmpty())
                ? request.getFileName()
                : "signed-document.pdf";

        return new PdfSignResponse(signedBase64, fileName);
    }

    @Override
    public PdfSignatureListResponse getSignaturesFromBase64(PdfSignBase64Request request) throws IOException {
        if (request == null || request.getPdfBase64() == null || request.getPdfBase64().isEmpty()) {
            throw new IllegalArgumentException("pdfBase64 is required");
        }

        byte[] pdfBytes;
        try {
            pdfBytes = Base64.getDecoder().decode(request.getPdfBase64());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 PDF content", e);
        }

        List<PdfSignatureInfo> signatures = extractSignatures(pdfBytes);

        if (signatures == null || signatures.isEmpty()) {
            return new PdfSignatureListResponse(
                    Collections.emptyList(),
                    "Document is not electronically signed"
            );
        }

        return new PdfSignatureListResponse(signatures, null);
    }

    @Override
    public PdfSignResponse signPdfFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }

        byte[] pdfBytes = file.getBytes();
        byte[] signedBytes = signPdf(pdfBytes);

        String signedBase64 = Base64.getEncoder().encodeToString(signedBytes);
        String originalName = file.getOriginalFilename();
        String fileName = (originalName != null && !originalName.isEmpty())
                ? originalName.replaceAll("(?i)\\.pdf$", "") + "_signed.pdf"
                : "signed-document.pdf";

        return new PdfSignResponse(signedBase64, fileName);
    }

    @Override
    public SignerInfoResponse getSignerInfo(String alias) {
        try {
            String effectiveAlias = (alias != null && !alias.isBlank()) ? alias : defaultAlias;

            if (!(certificate instanceof X509Certificate)) {
                throw new IllegalStateException("Configured certificate is not an X509 certificate");
            }

            X509Certificate x509 = (X509Certificate) certificate;

            String subjectDn = x509.getSubjectX500Principal().getName();
            String issuerDn = x509.getIssuerX500Principal().getName();
            String commonName = extractCommonName(subjectDn);

            String validFrom = x509.getNotBefore().toInstant()
                    .atZone(IST_ZONE)
                    .format(RESPONSE_DATE_FORMATTER);
            String validTo = x509.getNotAfter().toInstant()
                    .atZone(IST_ZONE)
                    .format(RESPONSE_DATE_FORMATTER);
            String serialNumber = x509.getSerialNumber().toString(16).toUpperCase();

            return new SignerInfoResponse(
                    effectiveAlias,
                    commonName,
                    subjectDn,
                    issuerDn,
                    validFrom,
                    validTo,
                    serialNumber
            );
        } catch (Exception e) {
            log.error("Failed to resolve signer info for alias: {}", alias, e);
            throw new RuntimeException("Failed to resolve signer info: " + e.getMessage(), e);
        }
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

    private byte[] createCmsSignature(ByteArrayInputStream contentStream)
            throws OperatorCreationException, CMSException, IOException {

        X509Certificate x509Certificate = (X509Certificate) certificate;
        List<java.security.cert.Certificate> certList = Collections.singletonList(x509Certificate);
        JcaCertStore certStore;
        try {
            certStore = new JcaCertStore(certList);
        } catch (java.security.cert.CertificateEncodingException e) {
            throw new IOException("Failed to encode certificate: " + e.getMessage(), e);
        }

        String signatureAlgorithm = "SHA256withRSA";
        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm)
                .build(privateKey);

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        try {
            generator.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(
                            new JcaDigestCalculatorProviderBuilder().build())
                            .build(contentSigner, x509Certificate)
            );
        } catch (java.security.cert.CertificateEncodingException e) {
            throw new IOException("Failed to encode certificate for signer info: " + e.getMessage(), e);
        }
        generator.addCertificates(certStore);

        byte[] contentBytes = contentStream.readAllBytes();
        CMSSignedData signedData = generator.generate(new CMSProcessableByteArray(contentBytes), false);
        return signedData.getEncoded();
    }
}


