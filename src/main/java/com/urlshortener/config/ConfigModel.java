package com.urlshortener.config;

/**
 * Модель конфигурации приложения
 */
public class ConfigModel {
    private StorageConfig storage;
    private UrlShortenerConfig url_shortener;
    private TTLServiceConfig ttl_service;

    public StorageConfig getStorage() {
        return storage;
    }

    public void setStorage(StorageConfig storage) {
        this.storage = storage;
    }

    public UrlShortenerConfig getUrl_shortener() {
        return url_shortener;
    }

    public void setUrl_shortener(UrlShortenerConfig url_shortener) {
        this.url_shortener = url_shortener;
    }

    public TTLServiceConfig getTtl_service() {
        return ttl_service;
    }

    public void setTtl_service(TTLServiceConfig ttl_service) {
        this.ttl_service = ttl_service;
    }
}
