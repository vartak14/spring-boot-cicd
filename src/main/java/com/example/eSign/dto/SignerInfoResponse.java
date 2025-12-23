package com.example.eSign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Signer details derived from the certificate stored in the keystore.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignerInfoResponse {

    /**
     * Alias used to load the certificate from the keystore.
     */
    private String alias;

    /**
     * Common Name (CN) of the subject, if present.
     */
    private String commonName;

    /**
     * Full subject distinguished name.
     */
    private String subjectDn;

    /**
     * Issuer distinguished name.
     */
    private String issuerDn;

    /**
     * Certificate validity start time in format yyyy-MM-dd HH:mm:ss (IST).
     */
    private String validFrom;

    /**
     * Certificate validity end time in format yyyy-MM-dd HH:mm:ss (IST).
     */
    private String validTo;

    /**
     * Certificate serial number as a hex string.
     */
    private String serialNumber;
}


