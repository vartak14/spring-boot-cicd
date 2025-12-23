package com.example.eSign.dto;

import lombok.Data;

@Data
public class PdfSignBase64Request {
    /**
     * Base64-encoded original PDF content.
     */
    private String pdfBase64;

    /**
     * Optional filename hint for response or logging.
     */
    private String fileName;
}


