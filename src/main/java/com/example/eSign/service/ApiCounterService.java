package com.example.eSign.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCounterService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "api:count:";

    public void increment(String method, String path) {
        String key = KEY_PREFIX + method + ":" + path;
        
        // Try to set key to 1 if it doesn't exist (atomic operation)
        Boolean wasAbsent = redisTemplate.opsForValue().setIfAbsent(key, "1");
        
        if (Boolean.FALSE.equals(wasAbsent)) {
            // Key already exists - increment it
            redisTemplate.opsForValue().increment(key);
            log.debug("Incremented counter for {} {}", method, path);
        } else {
            log.debug("Created new counter for {} {}", method, path);
        }
    }

    public Long getCount(String method, String path) {
        String key = KEY_PREFIX + method + ":" + path;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    public Map<String, Long> getAllCounts() {
        Map<String, Long> counts = new HashMap<>();
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null) {
            for (String key : keys) {
                String apiKey = key.replace(KEY_PREFIX, "");
                String value = redisTemplate.opsForValue().get(key);
                counts.put(apiKey, value != null ? Long.parseLong(value) : 0L);
            }
        }
        return counts;
    }
}

