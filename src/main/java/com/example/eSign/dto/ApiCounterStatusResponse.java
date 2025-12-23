package com.example.eSign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiCounterStatusResponse {
    private String status;
    private Long callCount;
    private String message;
}

