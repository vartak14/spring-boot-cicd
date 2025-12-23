package com.example.eSign.service.impl;

import com.example.eSign.dto.FileOperationRequest;
import com.example.eSign.dto.FileOperationResponse;
import com.example.eSign.service.FileOperationService;
import com.example.eSign.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class FileOperationServiceImpl implements FileOperationService {

    @Override
    public FileOperationResponse encodeFile(FileOperationRequest request) {
        String inputFileName = request.getInputFileName();
        String outputFileName = request.getOutputFileName();

        try {
            log.info("Encoding file: {} to {}", inputFileName, outputFileName);
            performEncode(inputFileName, outputFileName);

            return new FileOperationResponse(
                    Constants.SUCCESS,
                    Constants.FILE_ENCODED_SUCCESS,
                    inputFileName,
                    outputFileName,
                    null
            );
        } catch (IOException e) {
            log.error("Error encoding file: {}", e.getMessage());
            return new FileOperationResponse(
                    Constants.FAILURE,
                    null,
                    inputFileName,
                    outputFileName,
                    String.format(Constants.FILE_ENCODE_ERROR, e.getMessage())
            );
        } catch (Exception e) {
            log.error("Unexpected error during encoding: {}", e.getMessage());
            return new FileOperationResponse(
                    Constants.FAILURE,
                    null,
                    inputFileName,
                    outputFileName,
                    String.format(Constants.UNEXPECTED_ERROR, e.getMessage())
            );
        }
    }

    @Override
    public FileOperationResponse decodeFile(FileOperationRequest request) {
        String inputFileName = request.getInputFileName();
        String outputFileName = request.getOutputFileName();

        try {
            log.info("Decoding file: {} to {}", inputFileName, outputFileName);
            performDecode(inputFileName, outputFileName);

            return new FileOperationResponse(
                    Constants.SUCCESS,
                    Constants.FILE_DECODED_SUCCESS,
                    inputFileName,
                    outputFileName,
                    null
            );
        } catch (IOException e) {
            log.error("Error decoding file: {}", e.getMessage());
            return new FileOperationResponse(
                    Constants.FAILURE,
                    null,
                    inputFileName,
                    outputFileName,
                    String.format(Constants.FILE_DECODE_ERROR, e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid base64 data: {}", e.getMessage());
            return new FileOperationResponse(
                    Constants.FAILURE,
                    null,
                    inputFileName,
                    outputFileName,
                    String.format(Constants.FILE_INVALID_BASE64, e.getMessage())
            );
        } catch (Exception e) {
            log.error("Unexpected error during decoding: {}", e.getMessage());
            return new FileOperationResponse(
                    Constants.FAILURE,
                    null,
                    inputFileName,
                    outputFileName,
                    String.format(Constants.UNEXPECTED_ERROR, e.getMessage())
            );
        }
    }

    private void performEncode(String inputFileName, String outputFileName) throws IOException {
        File inputFile = new File(inputFileName);
        if (!inputFile.exists()) {
            throw new IOException(String.format(Constants.FILE_NOT_EXISTS, inputFileName));
        }

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFileName)) {

            byte[] fileBytes = fis.readAllBytes();
            String encodedString = Base64.getEncoder().encodeToString(fileBytes);
            fos.write(encodedString.getBytes());

            log.debug("Encoded {} bytes to base64", fileBytes.length);
        }
    }

    private void performDecode(String inputFileName, String outputFileName) throws IOException {
        File inputFile = new File(inputFileName);
        if (!inputFile.exists()) {
            throw new IOException(String.format(Constants.FILE_NOT_EXISTS, inputFileName));
        }

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFileName)) {

            String encodedString = new String(fis.readAllBytes());
            byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
            fos.write(decodedBytes);

            log.debug("Decoded base64 to {} bytes", decodedBytes.length);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(Constants.FILE_INVALID_BASE64_IN_FILE, inputFileName), e);
        }
    }
}


