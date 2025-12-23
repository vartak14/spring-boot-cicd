package com.example.eSign.service.impl;

import com.example.eSign.dto.ApiCounterStatusResponse;
import com.example.eSign.service.ApiCounterService;
import com.example.eSign.service.ApiCounterStatusService;
import com.example.eSign.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCounterStatusServiceImpl implements ApiCounterStatusService {

    private final ApiCounterService apiCounterService;

    @Override
    public ApiCounterStatusResponse getApiCounterStatus() {
        log.debug("Getting API counter status");
        Long count = apiCounterService.getCount(Constants.HTTP_METHOD_GET, Constants.API_COUNTER_PATH);
        log.info("API counter check - Application is running. Call count: {}", count);

        return new ApiCounterStatusResponse(
                Constants.API_COUNTER_STATUS,
                count,
                String.format(Constants.API_COUNTER_MESSAGE_TEMPLATE, count)
        );
    }
}

