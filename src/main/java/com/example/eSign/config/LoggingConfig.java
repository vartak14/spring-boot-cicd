package com.example.eSign.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    @Value("${app.logging.enabled:true}")
    private boolean loggingEnabled;

    @PostConstruct
    public void configureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Root logger
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        // Application package logger
        Logger appLogger = context.getLogger("com.example.eSign");

        if (loggingEnabled) {
            // Normal / verbose logging
            rootLogger.setLevel(Level.INFO);
            appLogger.setLevel(Level.DEBUG);
        } else {
            // Effectively turn off logs from the app
            rootLogger.setLevel(Level.ERROR);
            appLogger.setLevel(Level.OFF);
        }
    }
}


