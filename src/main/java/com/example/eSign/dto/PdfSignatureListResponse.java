package com.example.eSign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO for PDF signature extraction responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfSignatureListResponse {

    /**
     * List of signatures present on the document.
     */
    private List<PdfSignatureInfo> signatures;

    /**
     * Informational text about the signature state, if any.
     * For example: "Document is not electronically signed".
     */
    private String statusInfo;
}


