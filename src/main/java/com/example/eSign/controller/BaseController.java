package com.example.eSign.controller;

import com.example.eSign.dto.*;
import com.example.eSign.service.FileOperationService;
import com.example.eSign.service.ApiCounterStatusService;
import com.example.eSign.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/esign")
@RequiredArgsConstructor
public class BaseController {

    private final ApiCounterStatusService apiCounterStatusService;
    private final FileOperationService fileOperationService;

    @GetMapping("/api-counter")
    public ResponseEntity<ApiResponse<ApiCounterStatusResponse>> apiCounter() {
        log.debug("API counter endpoint called");
        ApiCounterStatusResponse apiCounterData = apiCounterStatusService.getApiCounterStatus();

        ApiResponse<ApiCounterStatusResponse> response = new ApiResponse<>(
                Constants.STATUS_SUCCESS,
                apiCounterData,
                null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/encode")
    public ResponseEntity<ApiResponse<FileOperationResponse>> encodeBase64(
            @RequestBody ApiRequest<FileOperationRequest> request) {
        FileOperationRequest fileRequest = request.getData();
        
        if (fileRequest == null || fileRequest.getInputFileName() == null || fileRequest.getOutputFileName() == null) {
            List<String> messages = Collections.singletonList(Constants.FILE_FIELDS_REQUIRED);
            ApiResponse<FileOperationResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_BAD_REQUEST).body(response);
        }

        FileOperationResponse fileData = fileOperationService.encodeFile(fileRequest);
        
        if (Boolean.TRUE.equals(fileData.getSuccess())) {
            ApiResponse<FileOperationResponse> response = new ApiResponse<>(
                    Constants.STATUS_SUCCESS,
                    fileData,
                    null);
            return ResponseEntity.ok(response);
        } else {
            List<String> messages = Collections.singletonList(fileData.getError());
            ApiResponse<FileOperationResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/decode")
    public ResponseEntity<ApiResponse<FileOperationResponse>> decodeBase64(
            @RequestBody ApiRequest<FileOperationRequest> request) {
        FileOperationRequest fileRequest = request.getData();
        
        if (fileRequest == null || fileRequest.getInputFileName() == null || fileRequest.getOutputFileName() == null) {
            List<String> messages = Collections.singletonList(Constants.FILE_FIELDS_REQUIRED);
            ApiResponse<FileOperationResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(Constants.HTTP_STATUS_BAD_REQUEST).body(response);
        }

        FileOperationResponse fileData = fileOperationService.decodeFile(fileRequest);
        
        if (Boolean.TRUE.equals(fileData.getSuccess())) {
            ApiResponse<FileOperationResponse> response = new ApiResponse<>(
                    Constants.STATUS_SUCCESS,
                    fileData,
                    null);
            return ResponseEntity.ok(response);
        } else {
            int statusCode = fileData.getError() != null && fileData.getError().contains("Invalid base64") 
                    ? Constants.HTTP_STATUS_BAD_REQUEST 
                    : Constants.HTTP_STATUS_INTERNAL_SERVER_ERROR;
            List<String> messages = Collections.singletonList(fileData.getError());
            ApiResponse<FileOperationResponse> response = new ApiResponse<>(
                    Constants.STATUS_ERROR,
                    null,
                    messages);
            return ResponseEntity.status(statusCode).body(response);
        }
    }
}
