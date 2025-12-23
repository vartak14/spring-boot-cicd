package com.example.eSign.service;

import com.example.eSign.dto.FileOperationRequest;
import com.example.eSign.dto.FileOperationResponse;

public interface FileOperationService {
    
    /**
     * Encode a file to base64
     * @param request File operation request with input and output file names
     * @return FileOperationResponse with operation result
     */
    FileOperationResponse encodeFile(FileOperationRequest request);
    
    /**
     * Decode a base64 encoded file
     * @param request File operation request with input and output file names
     * @return FileOperationResponse with operation result
     */
    FileOperationResponse decodeFile(FileOperationRequest request);
}


