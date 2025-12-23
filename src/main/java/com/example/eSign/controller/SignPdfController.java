package com.example.eSign.controller;

import com.example.eSign.dto.ApiRequest;
import com.example.eSign.dto.ApiResponse;
import com.example.eSign.dto.PdfSignBase64Request;
import com.example.eSign.dto.PdfSignResponse;
import com.example.eSign.dto.PdfSignatureListResponse;
import com.example.eSign.dto.SignerInfoResponse;
import com.example.eSign.service.PdfSigningService;
import com.example.eSign.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/esign")
@RequiredArgsConstructor
public class SignPdfController {

    private final PdfSigningService pdfSigningService;

    @PostMapping("/sign-pdf/base64")
    public ResponseEntity<ApiResponse<PdfSignResponse>> signPdfBase64(
            @RequestBody ApiRequest<PdfSignBase64Request> request) {

        try {
            PdfSignBase64Request data = request.getData();
            PdfSignResponse payload = pdfSigningService.signPdfFromBase64(data);
            ApiResponse<PdfSignResponse> response = new ApiResponse<>(
                    Constants.STATUS_SUCCESS,
                    payload,
                    null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            List<String> messages = Collections.singletonList(e.getMessage());
            ApiResponse<PdfSignResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_BAD_REQUEST).body(response);
        } catch (IOException e) {
            log.error("Failed to sign PDF from base64", e);
            List<String> messages = Collections.singletonList("Failed to sign PDF: " + e.getMessage());
            ApiResponse<PdfSignResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/signatures/base64")
    public ResponseEntity<ApiResponse<PdfSignatureListResponse>> getSignaturesFromBase64(
            @RequestBody ApiRequest<PdfSignBase64Request> request) {

        try {
            PdfSignBase64Request data = request.getData();
            PdfSignatureListResponse payload = pdfSigningService.getSignaturesFromBase64(data);

            // If there are no signatures, treat as error with message from payload
            if (payload.getSignatures() == null || payload.getSignatures().isEmpty()) {
                ApiResponse<PdfSignatureListResponse> response = new ApiResponse<>(
                        Constants.STATUS_ERROR,
                        payload,
                        null);
                return ResponseEntity.status(Constants.HTTP_STATUS_BAD_REQUEST).body(response);
            }

            ApiResponse<PdfSignatureListResponse> response = new ApiResponse<>(
                    Constants.STATUS_SUCCESS,
                    payload,
                    null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            List<String> messages = Collections.singletonList(e.getMessage());
            ApiResponse<PdfSignatureListResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_BAD_REQUEST).body(response);
        } catch (IOException e) {
            log.error("Failed to extract signatures from base64 PDF", e);
            List<String> messages = Collections.singletonList("Failed to extract signatures: " + e.getMessage());
            ApiResponse<PdfSignatureListResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/sign-pdf/file")
    public ResponseEntity<ApiResponse<PdfSignResponse>> signPdfFile(
            @RequestParam("file") MultipartFile file) {

        try {
            PdfSignResponse payload = pdfSigningService.signPdfFile(file);
            ApiResponse<PdfSignResponse> response = new ApiResponse<>(
                    Constants.STATUS_SUCCESS,
                    payload,
                    null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            List<String> messages = Collections.singletonList(e.getMessage());
            ApiResponse<PdfSignResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_BAD_REQUEST).body(response);
        } catch (IOException e) {
            log.error("Failed to sign uploaded PDF file", e);
            List<String> messages = Collections.singletonList("Failed to sign PDF: " + e.getMessage());
            ApiResponse<PdfSignResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Returns signer details for a certificate stored in the keystore.
     * <p>
     * If {@code alias} is not provided, the default alias from configuration is used.
     */
    @PostMapping("/signer-info")
    public ResponseEntity<ApiResponse<SignerInfoResponse>> getSignerInfo(
            @RequestParam(value = "alias", required = false) String alias) {

        try {
            SignerInfoResponse payload = pdfSigningService.getSignerInfo(alias);
            ApiResponse<SignerInfoResponse> response = new ApiResponse<>(
                    Constants.STATUS_SUCCESS,
                    payload,
                    null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to read signer info", e);
            List<String> messages = Collections.singletonList("Failed to read signer info: " + e.getMessage());
            ApiResponse<SignerInfoResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
