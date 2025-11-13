package com.urlshortener.service;

import com.urlshortener.config.Config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для автоматического удаления истекших ссылок
 */
public class TTLService {
    private final UrlShortenerService urlShortenerService;
    private final ScheduledExecutorService scheduler;
    private final Config config = Config.getInstance();

    public TTLService(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Запускает периодическую очистку истекших ссылок
     */
    public void start() {
        scheduler.scheduleAtFixedRate(
                this::cleanupExpiredLinks,
                0,
                config.getCleanupIntervalMinutes(),
                TimeUnit.MINUTES);
    }

    /**
     * Останавливает сервис очистки
     */
    public void stop() {
        scheduler.shutdown();
    }

    private void cleanupExpiredLinks() {
        urlShortenerService.removeExpiredLinks();
    }
}
