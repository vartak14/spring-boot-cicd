package com.example.eSign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single digital signature present on a PDF document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfSignatureInfo {

    /**
     * The name of the signer, if available.
     */
    private String name;

    /**
     * The signing location, if available.
     */
    private String location;

    /**
     * The signing reason, if available.
     */
    private String reason;

    /**
     * The signing date/time in format yyyy-MM-dd HH:mm:ss, if available.
     */
    private String signDate;

    /**
     * The signature sub-filter (e.g. adbe.pkcs7.detached), if available.
     */
    private String subFilter;
}


