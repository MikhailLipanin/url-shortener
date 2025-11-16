package com.urlshortener.config;

/**
 * Модель конфигурации приложения
 */
public class ConfigModel {
    private StorageConfig storage;
    private UrlShortenerConfig urlShortener;
    private TTLServiceConfig ttlService;

    public StorageConfig getStorage() {
        return storage;
    }

    public void setStorage(StorageConfig storage) {
        this.storage = storage;
    }

    public UrlShortenerConfig getUrlShortener() {
        return urlShortener;
    }

    public void setUrlShortener(UrlShortenerConfig urlShortener) {
        this.urlShortener = urlShortener;
    }

    public TTLServiceConfig getTtlService() {
        return ttlService;
    }

    public void setTtlService(TTLServiceConfig ttlService) {
        this.ttlService = ttlService;
    }
}
