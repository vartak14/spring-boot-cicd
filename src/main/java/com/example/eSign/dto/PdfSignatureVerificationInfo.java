package com.example.eSign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed verification result for a single digital signature on a PDF.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfSignatureVerificationInfo {

    /**
     * Signer name, if it could be resolved from the certificate or PDF signature.
     */
    private String signerName;

    /**
     * Signing date/time in format yyyy-MM-dd HH:mm:ss, if available.
     */
    private String signingTime;

    /**
     * Whether the cryptographic signature and signed byte range are intact.
     */
    private boolean integrityValid;

    /**
     * Whether the signer certificate is within its validity period.
     */
    private boolean certificateValid;

    /**
     * Whether the signer is considered trusted according to server-side rules.
     */
    private boolean trusted;

    /**
     * Optional message describing validation details or errors.
     */
    private String message;
}


