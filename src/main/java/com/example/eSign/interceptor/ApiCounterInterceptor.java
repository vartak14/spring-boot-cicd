package com.example.eSign.interceptor;

import com.example.eSign.service.ApiCounterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCounterInterceptor implements HandlerInterceptor {

    private final ApiCounterService apiCounterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        log.debug("Intercepting API call: {} {}", method, path);
        apiCounterService.increment(method, path);
        return true;
    }
}


