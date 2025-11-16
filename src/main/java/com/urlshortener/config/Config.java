package com.urlshortener.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;

/**
 * Класс для загрузки конфигурации из YAML файла
 */
public class Config {
    private static Config instance;
    private final ConfigModel configModel;
    private static String configPath = null;

    // Устанавливает путь к конфигурационному файлу
    public static void setConfigPath(String path) {
        configPath = path;
        instance = null;
    }

    private Config() {
        Yaml yaml = new Yaml();
        ConfigModel loadedConfig = null;

        // Если указан путь через setConfigPath, используем его
        if (configPath != null) {
            try (InputStream inputStream = new java.io.FileInputStream(configPath)) {
                loadedConfig = yaml.loadAs(inputStream, ConfigModel.class);
            } catch (Exception e) {
                throw new RuntimeException("Не удалось загрузить конфигурацию из " + configPath + ": " + e.getMessage(),
                        e);
            }
        } else {
            // Пробуем загрузить из дефолтного места (корень проекта)
            File defaultConfig = new File("config.yaml");
            if (defaultConfig.exists()) {
                try (InputStream inputStream = new java.io.FileInputStream(defaultConfig)) {
                    loadedConfig = yaml.loadAs(inputStream, ConfigModel.class);
                } catch (Exception e) {
                    throw new RuntimeException("Не удалось загрузить конфигурацию из " + defaultConfig.getAbsolutePath()
                            + ": " + e.getMessage(), e);
                }
            }
        }

        if (loadedConfig == null) {
            throw new RuntimeException("Не удалось загрузить конфигурацию.");
        }

        this.configModel = loadedConfig;
    }

    public static Config getInstance() {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    // Storage config
    public String getStorageDirectory() {
        return configModel.getStorage().getDirectory();
    }

    public String getUserLinksFile() {
        return configModel.getStorage().getUserLinksFile();
    }

    public String getActiveUsersFile() {
        return configModel.getStorage().getActiveUsersFile();
    }

    // URL Shortener config
    public String getBaseUrl() {
        return configModel.getUrlShortener().getBaseUrl();
    }

    public long getDefaultTtlHours() {
        return configModel.getUrlShortener().getDefaultTtlHours();
    }

    public int getCodeLength() {
        return configModel.getUrlShortener().getCodeLength();
    }

    public String getAlphabet() {
        return configModel.getUrlShortener().getAlphabet();
    }

    // TTL Service config
    public long getCleanupIntervalMinutes() {
        return configModel.getTtlService().getCleanupIntervalMinutes();
    }
}
