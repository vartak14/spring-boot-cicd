package com.example.eSign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileOperationResponse {
    private Boolean success;
    private String message;
    private String inputFile;
    private String outputFile;
    private String error;
}


