package com.urlshortener.config;

/**
 * Конфигурация сервиса сокращения URL
 */
public class UrlShortenerConfig {
    private String baseUrl;
    private long defaultTtlHours;
    private int codeLength;
    private String alphabet;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getDefaultTtlHours() {
        return defaultTtlHours;
    }

    public void setDefaultTtlHours(long defaultTtlHours) {
        this.defaultTtlHours = defaultTtlHours;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }
}
