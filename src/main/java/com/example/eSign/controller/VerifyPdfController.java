package com.example.eSign.controller;

import com.example.eSign.dto.ApiRequest;
import com.example.eSign.dto.ApiResponse;
import com.example.eSign.dto.PdfSignBase64Request;
import com.example.eSign.dto.PdfVerificationResult;
import com.example.eSign.service.PdfVerificationService;
import com.example.eSign.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/esign/verify")
@RequiredArgsConstructor
public class VerifyPdfController {

    private final PdfVerificationService pdfVerificationService;

    /**
     * Verifies digital signatures present on a base64-encoded PDF.
     */
    @PostMapping("/base64")
    public ResponseEntity<ApiResponse<PdfVerificationResult>> verifyPdfBase64(
            @RequestBody ApiRequest<PdfSignBase64Request> request) {

        try {
            PdfSignBase64Request data = request.getData();
            PdfVerificationResult payload = pdfVerificationService.verifyPdfFromBase64(data);
            ApiResponse<PdfVerificationResult> response = new ApiResponse<>(
                    Constants.STATUS_SUCCESS,
                    payload,
                    null);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            List<String> messages = Collections.singletonList(e.getMessage());
            ApiResponse<PdfVerificationResult> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_BAD_REQUEST).body(response);
        } catch (IOException e) {
            log.error("Failed to verify base64 PDF signatures", e);
            List<String> messages = Collections.singletonList("Failed to verify PDF: " + e.getMessage());
            ApiResponse<PdfVerificationResult> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("Unexpected error while verifying base64 PDF signatures", e);
            List<String> messages = Collections.singletonList("Unexpected error while verifying PDF: " + e.getMessage());
            ApiResponse<PdfVerificationResult> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


