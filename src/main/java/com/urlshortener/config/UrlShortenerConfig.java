package com.urlshortener.config;

/**
 * Конфигурация сервиса сокращения URL
 */
public class UrlShortenerConfig {
    private String base_url;
    private long default_ttl_hours;
    private int code_length;
    private String alphabet;

    public String getBase_url() {
        return base_url;
    }

    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }

    public long getDefault_ttl_hours() {
        return default_ttl_hours;
    }

    public void setDefault_ttl_hours(long default_ttl_hours) {
        this.default_ttl_hours = default_ttl_hours;
    }

    public int getCode_length() {
        return code_length;
    }

    public void setCode_length(int code_length) {
        this.code_length = code_length;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }
}
