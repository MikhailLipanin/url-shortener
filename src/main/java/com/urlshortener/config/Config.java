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

    /**
     * Устанавливает путь к конфигурационному файлу
     * 
     * @param path путь к config.yaml
     */
    public static void setConfigPath(String path) {
        configPath = path;
        // Сбрасываем instance, чтобы перезагрузить конфиг
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
            } else {
                // Пробуем загрузить из ресурсов
                try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("config.yaml")) {
                    if (inputStream != null) {
                        loadedConfig = yaml.loadAs(inputStream, ConfigModel.class);
                    }
                } catch (Exception e) {
                    // Игнорируем
                }
            }
        }

        if (loadedConfig == null) {
            throw new RuntimeException(
                    "Не удалось загрузить конфигурацию. Убедитесь, что файл config.yaml существует в корне проекта или укажите путь через -Dconfig.path=...");
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
        return configModel.getStorage().getUser_links_file();
    }

    public String getActiveUsersFile() {
        return configModel.getStorage().getActive_users_file();
    }

    // URL Shortener config
    public String getBaseUrl() {
        return configModel.getUrl_shortener().getBase_url();
    }

    public long getDefaultTtlHours() {
        return configModel.getUrl_shortener().getDefault_ttl_hours();
    }

    public int getCodeLength() {
        return configModel.getUrl_shortener().getCode_length();
    }

    public String getAlphabet() {
        return configModel.getUrl_shortener().getAlphabet();
    }

    // TTL Service config
    public long getCleanupIntervalMinutes() {
        return configModel.getTtl_service().getCleanup_interval_minutes();
    }
}
