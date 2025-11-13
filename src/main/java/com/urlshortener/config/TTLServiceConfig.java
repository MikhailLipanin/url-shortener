package com.urlshortener.config;

/**
 * Конфигурация сервиса TTL
 */
public class TTLServiceConfig {
    private long cleanup_interval_minutes;

    public long getCleanup_interval_minutes() {
        return cleanup_interval_minutes;
    }

    public void setCleanup_interval_minutes(long cleanup_interval_minutes) {
        this.cleanup_interval_minutes = cleanup_interval_minutes;
    }
}
