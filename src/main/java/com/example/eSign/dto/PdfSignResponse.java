package com.example.eSign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfSignResponse {
    /**
     * Base64-encoded signed PDF content.
     */
    private String signedPdfBase64;

    /**
     * Suggested filename for the signed PDF.
     */
    private String fileName;
}


