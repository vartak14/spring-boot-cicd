package com.example.eSign.util;

public class Constants {
    
    // API Paths
    public static final String API_V1 = "/api/v1";
    public static final String API_COUNTER_PATH = "/esign/api-counter";
    public static final String HTTP_METHOD_GET = "GET";
    
    // API Counter Messages
    public static final String API_COUNTER_STATUS = "Application is running";
    public static final String API_COUNTER_MESSAGE_TEMPLATE = "This endpoint has been called %d time(s)";
    
    // File Operation Messages
    public static final String FILE_ENCODED_SUCCESS = "File encoded successfully";
    public static final String FILE_DECODED_SUCCESS = "File decoded successfully";
    public static final String FILE_FIELDS_REQUIRED = "inputFileName and outputFileName are required";
    public static final String FILE_ENCODE_ERROR = "Failed to encode file: %s";
    public static final String FILE_DECODE_ERROR = "Failed to decode file: %s";
    public static final String FILE_INVALID_BASE64 = "Invalid base64 data: %s";
    public static final String FILE_INVALID_BASE64_IN_FILE = "Invalid base64 data in file: %s";
    public static final String FILE_NOT_EXISTS = "Input file does not exist: %s";
    public static final String UNEXPECTED_ERROR = "Unexpected error: %s";
    
    // HTTP Status Codes
    public static final int HTTP_STATUS_BAD_REQUEST = 400;
    public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;
    
    // Boolean Values
    public static final Boolean SUCCESS = true;
    public static final Boolean FAILURE = false;
    
    // Response Status
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_FAILURE = "FAILURE";
    
    private Constants() {
        // Utility class
    }
}



