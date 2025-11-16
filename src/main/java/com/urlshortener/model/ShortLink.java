package com.urlshortener.model;

import java.time.LocalDateTime;

/**
 * Модель укороченной ссылки
 */
public class ShortLink {
    private String shortCode;
    private String originalUrl;
    private String userNickname;
    private int clickCount;
    private Integer clickLimit;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isActive;

    public ShortLink(String shortCode, String originalUrl, String userNickname, Integer clickLimit,
            LocalDateTime expiresAt) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.userNickname = userNickname;
        this.clickCount = 0;
        this.clickLimit = clickLimit;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.isActive = true;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void incrementClickCount() {
        this.clickCount++;
    }

    public Integer getClickLimit() {
        return clickLimit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isClickLimitExceeded() {
        return clickLimit != null && clickCount >= clickLimit;
    }

    public boolean canBeAccessed() {
        return isActive && !isExpired() && !isClickLimitExceeded();
    }

    @Override
    public String toString() {
        return String.format("ShortLink{code='%s', url='%s', clicks=%d/%s, expires=%s, active=%s}",
                shortCode, originalUrl, clickCount,
                clickLimit != null ? clickLimit.toString() : "unlimited",
                expiresAt, isActive);
    }
}
