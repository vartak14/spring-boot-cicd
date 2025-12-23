package com.example.eSign.service;

import com.example.eSign.dto.PdfSignBase64Request;
import com.example.eSign.dto.PdfSignResponse;
import com.example.eSign.dto.PdfSignatureInfo;
import com.example.eSign.dto.PdfSignatureListResponse;
import com.example.eSign.dto.SignerInfoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PdfSigningService {

    /**
     * Sign a PDF provided as raw bytes and return the signed PDF bytes.
     *
     * @param pdfBytes original PDF bytes
     * @return signed PDF bytes
     * @throws IOException if PDF processing fails
     */
    byte[] signPdf(byte[] pdfBytes) throws IOException;

    /**
     * Extract digital signatures present on a PDF document.
     *
     * @param pdfBytes PDF bytes (typically a signed PDF)
     * @return list of signatures found on the document
     * @throws IOException if PDF processing fails
     */
    List<PdfSignatureInfo> extractSignatures(byte[] pdfBytes) throws IOException;

    /**
     * Sign a PDF provided as base64 in the request and return response DTO.
     *
     * @param request base64 PDF request
     * @return signing response payload
     */
    PdfSignResponse signPdfFromBase64(PdfSignBase64Request request) throws IOException;

    /**
     * Extract signatures from a base64 PDF request and return response DTO.
     *
     * @param request base64 PDF request
     * @return list response payload, including message when unsigned
     */
    PdfSignatureListResponse getSignaturesFromBase64(PdfSignBase64Request request) throws IOException;

    /**
     * Sign a PDF uploaded as multipart file and return response DTO.
     *
     * @param file multipart PDF file
     * @return signing response payload
     */
    PdfSignResponse signPdfFile(MultipartFile file) throws IOException;

    /**
     * Returns signer details derived from the certificate stored in the keystore.
     *
     * @param alias optional keystore alias; if null/blank, implementation may use a default alias
     * @return signer information for the resolved certificate
     */
    SignerInfoResponse getSignerInfo(String alias);
}


