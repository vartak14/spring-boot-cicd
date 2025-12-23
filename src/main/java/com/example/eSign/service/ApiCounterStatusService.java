package com.example.eSign.service;

import com.example.eSign.dto.ApiCounterStatusResponse;

public interface ApiCounterStatusService {
    
    /**
     * Get API counter status with call count
     * @return ApiCounterStatusResponse with status and call count
     */
    ApiCounterStatusResponse getApiCounterStatus();
}

