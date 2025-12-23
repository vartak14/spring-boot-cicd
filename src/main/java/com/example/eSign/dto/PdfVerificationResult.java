package com.example.eSign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Overall verification result for a PDF document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfVerificationResult {

    /**
     * Whether all signatures on the document passed verification.
     * If the document has no signatures, this will typically be false.
     */
    private boolean overallValid;

    /**
     * Total number of signatures discovered on the PDF.
     */
    private int signatureCount;

    /**
     * Human-readable message summarising verification.
     */
    private String message;

    /**
     * Per-signature verification details.
     */
    private List<PdfSignatureVerificationInfo> signatures;
}


