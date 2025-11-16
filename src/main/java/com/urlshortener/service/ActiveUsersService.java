package com.urlshortener.service;

import com.urlshortener.config.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Сервис для управления счетчиком активных пользователей
 */
public class ActiveUsersService {
    private final Config config = Config.getInstance();
    private final Path activeUsersPath;

    public ActiveUsersService() {
        Path storagePath = Paths.get(config.getStorageDirectory());
        this.activeUsersPath = storagePath.resolve(config.getActiveUsersFile());

        // Создаем директорию, если её нет
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для хранения данных: " + e.getMessage(), e);
        }
    }

    /**
     * Увеличивает счетчик активных пользователей на 1
     * Если файла нет, создает его с значением 1
     */
    public void incrementActiveUsers() {
        int currentCount = getActiveUsersCount();
        int newCount = currentCount + 1;
        saveActiveUsersCount(newCount);
    }

    // Уменьшает счетчик активных пользователей на 1
    public void decrementActiveUsers() {
        int currentCount = getActiveUsersCount();
        if (currentCount > 0) {
            saveActiveUsersCount(currentCount - 1);
        }
    }

    // Получает текущее количество активных пользователей
    public int getActiveUsersCount() {
        if (!Files.exists(activeUsersPath)) {
            return 0;
        }

        try {
            String content = Files.readString(activeUsersPath).trim();
            // Извлекаем число из строки "Current number of active users: <число>"
            if (content.contains(":")) {
                String numberPart = content.substring(content.lastIndexOf(":") + 1).trim();
                return Integer.parseInt(numberPart);
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Ошибка при чтении файла активных пользователей: " + e.getMessage());
            return 0;
        }
    }

    // Сохраняет количество активных пользователей в файл
    private void saveActiveUsersCount(int count) {
        try {
            String content = "Current number of active users: " + count;
            Files.writeString(activeUsersPath, content);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении количества активных пользователей: " + e.getMessage(), e);
        }
    }
}
