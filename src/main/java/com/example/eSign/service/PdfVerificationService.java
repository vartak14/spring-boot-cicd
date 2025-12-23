package com.example.eSign.service;

import com.example.eSign.dto.PdfSignBase64Request;
import com.example.eSign.dto.PdfVerificationResult;

import java.io.IOException;

/**
 * Service responsible for verifying PDF digital signatures.
 */
public interface PdfVerificationService {

    /**
     * Verify signatures present on a base64-encoded PDF.
     *
     * @param request base64 PDF request
     * @return verification result with per-signature details
     */
    PdfVerificationResult verifyPdfFromBase64(PdfSignBase64Request request) throws IOException;
}


