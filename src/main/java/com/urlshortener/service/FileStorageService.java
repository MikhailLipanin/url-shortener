package com.urlshortener.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.urlshortener.config.Config;
import com.urlshortener.model.ShortLink;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для сохранения и загрузки данных в JSON файлы
 * Структура: nickname -> Map<URL, ShortLink>
 * Потокобезопасный доступ через FileLock
 */
public class FileStorageService {
    private final Config config = Config.getInstance();
    private final Path storagePath;
    private final Path userLinksPath;
    private final Gson gson;

    public FileStorageService() {
        this.storagePath = Paths.get(config.getStorageDirectory());
        this.userLinksPath = storagePath.resolve(config.getUserLinksFile());

        // Настраиваем Gson с адаптерами для LocalDateTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();

        // Создаем директорию, если её нет
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для хранения данных: " + e.getMessage(), e);
        }
    }

    // Сохраняет связи nickname -> Map<URL, ShortLink> с блокировкой файла
    public void saveUserLinks(Map<String, Map<String, ShortLink>> userLinks) {
        try (FileChannel channel = FileChannel.open(userLinksPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            // Блокируем файл для записи
            try (FileLock lock = channel.lock()) {
                try (Writer writer = new OutputStreamWriter(
                        new FileOutputStream(userLinksPath.toFile()))) {
                    gson.toJson(userLinks, writer);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении связей пользователей: " + e.getMessage(), e);
        }
    }

    // Загружает связи nickname -> Map<URL, ShortLink> с блокировкой файла
    public Map<String, Map<String, ShortLink>> loadUserLinks() {
        if (!Files.exists(userLinksPath)) {
            return new ConcurrentHashMap<>();
        }

        try (FileChannel channel = FileChannel.open(userLinksPath, StandardOpenOption.READ)) {
            // Блокируем файл для чтения
            try (FileLock lock = channel.lock(0, Long.MAX_VALUE, true)) {
                try (Reader reader = new FileReader(userLinksPath.toFile())) {
                    TypeToken<Map<String, Map<String, ShortLink>>> typeToken = new TypeToken<Map<String, Map<String, ShortLink>>>() {
                    };
                    Map<String, Map<String, ShortLink>> loaded = gson.fromJson(reader, typeToken.getType());
                    return loaded != null ? new ConcurrentHashMap<>(loaded) : new ConcurrentHashMap<>();
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке связей пользователей: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
}
