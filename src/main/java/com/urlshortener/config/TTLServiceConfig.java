package com.urlshortener.config;

/**
 * Конфигурация сервиса TTL
 */
public class TTLServiceConfig {
    private long cleanupIntervalMinutes;

    public long getCleanupIntervalMinutes() {
        return cleanupIntervalMinutes;
    }

    public void setCleanupIntervalMinutes(long cleanupIntervalMinutes) {
        this.cleanupIntervalMinutes = cleanupIntervalMinutes;
    }
}
