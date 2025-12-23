package com.example.eSign.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

@Slf4j
@Configuration
public class KeystoreConfig {

    @Value("${app.keystore.file}")
    private Resource keystoreFile;

    @Value("${app.keystore.password}")
    private String keystorePassword;

    @Value("${app.keystore.alias}")
    private String keystoreAlias;

    @Value("${app.keystore.key-password}")
    private String keyPassword;

    @Bean
    public KeyStore keyStore() {
        try {
            // Validate that keystore file path is provided
            if (keystoreFile == null || !keystoreFile.exists()) {
                String filePath = keystoreFile != null ? keystoreFile.getDescription() : "null";
                throw new IllegalArgumentException("Keystore file not found or not specified: " + filePath + 
                    ". Please set ESIGN_KEYSTORE_FILE environment variable.");
            }
            
            // Validate required properties
            if (keystorePassword == null || keystorePassword.isEmpty()) {
                throw new IllegalArgumentException("Keystore password is required. Please set ESIGN_KEYSTORE_PASSWORD environment variable.");
            }
            
            if (keystoreAlias == null || keystoreAlias.isEmpty()) {
                throw new IllegalArgumentException("Keystore alias is required. Please set ESIGN_KEYSTORE_ALIAS environment variable.");
            }
            
            String fileDescription = keystoreFile.getDescription();
            log.info("Loading keystore from: {}", fileDescription);
            
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            
            try (InputStream inputStream = keystoreFile.getInputStream()) {
                keyStore.load(inputStream, keystorePassword.toCharArray());
            }
            
            log.info("Keystore loaded successfully. Alias: {}", keystoreAlias);
            return keyStore;
        } catch (IllegalArgumentException e) {
            log.error("Keystore configuration error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            String fileDescription = keystoreFile != null ? keystoreFile.getDescription() : "null";
            log.error("Failed to load keystore from: {}", fileDescription, e);
            throw new RuntimeException("Failed to load keystore: " + e.getMessage(), e);
        }
    }

    @Bean
    public PrivateKey privateKey(KeyStore keyStore) {
        try {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keystoreAlias, keyPassword.toCharArray());
            if (privateKey == null) {
                throw new RuntimeException("Private key not found for alias: " + keystoreAlias);
            }
            log.info("Private key loaded successfully for alias: {}", keystoreAlias);
            return privateKey;
        } catch (Exception e) {
            log.error("Failed to load private key for alias: {}", keystoreAlias, e);
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    @Bean
    public Certificate certificate(KeyStore keyStore) {
        try {
            Certificate certificate = keyStore.getCertificate(keystoreAlias);
            if (certificate == null) {
                throw new RuntimeException("Certificate not found for alias: " + keystoreAlias);
            }
            log.info("Certificate loaded successfully for alias: {}", keystoreAlias);
            return certificate;
        } catch (Exception e) {
            log.error("Failed to load certificate for alias: {}", keystoreAlias, e);
            throw new RuntimeException("Failed to load certificate", e);
        }
    }
}

