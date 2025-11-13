package com.urlshortener.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель пользователя
 */
public class User {
    private UUID id;
    private LocalDateTime createdAt;

    public User(UUID id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", created=" + createdAt + "}";
    }
}
