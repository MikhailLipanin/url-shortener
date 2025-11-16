package com.urlshortener.service;

import com.urlshortener.config.Config;
import com.urlshortener.model.ShortLink;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Сервис для управления короткими ссылками
 * Работает с никнеймами для идентификации пользователей
 * Все операции выполняются напрямую с файлом, без кеширования
 */
public class UrlShortenerService {
    private final Config config = Config.getInstance();
    private final SecureRandom random = new SecureRandom();

    private final FileStorageService storageService;

    // Конструктор
    public UrlShortenerService() {
        this.storageService = new FileStorageService();
    }

    // Загружает данные из файла
    private Map<String, Map<String, ShortLink>> loadUserLinks() {
        return storageService.loadUserLinks();
    }

    // Сохраняет данные в файл
    private void saveUserLinks(Map<String, Map<String, ShortLink>> userLinks) {
        try {
            storageService.saveUserLinks(userLinks);
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    // Валидирует URL
    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL не может быть пустым");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Некорректный формат URL: " + e.getMessage());
        }
    }

    // Генерирует уникальный короткий код на основе никнейма и URL
    private String generateShortCode(String userNickname, String originalUrl) {
        String alphabet = config.getAlphabet();
        int codeLength = config.getCodeLength();

        // Используем хеш от userNickname и URL для обеспечения уникальности
        String combined = userNickname + originalUrl;
        int hash = combined.hashCode();

        // Генерируем код на основе хеша и случайного компонента
        StringBuilder code = new StringBuilder();
        int seed = Math.abs(hash) + random.nextInt(1000000);

        for (int i = 0; i < codeLength; i++) {
            code.append(alphabet.charAt(seed % alphabet.length()));
            seed = seed / alphabet.length();
        }

        return code.toString();
    }

    // Генерирует случайный короткий код
    private String generateRandomCode() {
        String alphabet = config.getAlphabet();
        int codeLength = config.getCodeLength();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return code.toString();
    }

    // Генерирует уникальный код для пользователя и URL
    private String generateUniqueCode(String userNickname, String originalUrl) {
        String code;
        int attempts = 0;

        // Загружаем данные из файла для проверки уникальности
        Map<String, Map<String, ShortLink>> userLinks = loadUserLinks();
        Set<String> existingCodes = getAllExistingCodes(userLinks);

        do {
            code = generateShortCode(userNickname, originalUrl);
            attempts++;
            // Если код уже существует, добавляем случайный компонент
            if (existingCodes.contains(code) && attempts < 10) {
                code = generateShortCode(userNickname, originalUrl + System.currentTimeMillis());
            }
        } while (existingCodes.contains(code) && attempts < 10);

        // Если все еще не уникален, генерируем полностью случайный
        if (existingCodes.contains(code)) {
            code = generateRandomCode() + generateRandomCode().substring(0, 2);
        }

        return code;
    }

    // Получает все существующие коды из всех пользователей
    private Set<String> getAllExistingCodes(Map<String, Map<String, ShortLink>> userLinks) {
        Set<String> codes = new HashSet<>();
        for (Map<String, ShortLink> userMap : userLinks.values()) {
            for (ShortLink link : userMap.values()) {
                codes.add(link.getShortCode());
            }
        }
        return codes;
    }

    // Создает короткую ссылку для пользователя по никнейму
    public String createShortLink(String originalUrl, String userNickname, Integer clickLimit) {
        // Проверяем валидность URL
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL не может быть пустым");
        }

        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        // Валидируем URL
        validateUrl(originalUrl);

        // Загружаем данные из файла
        Map<String, Map<String, ShortLink>> userLinks = loadUserLinks();

        // Инициализируем мапу для пользователя, если её нет
        if (!userLinks.containsKey(userNickname)) {
            userLinks.put(userNickname, new HashMap<>());
        }

        // Проверяем, не существует ли уже ссылка для этого URL у этого пользователя
        Map<String, ShortLink> userMap = userLinks.get(userNickname);
        if (userMap.containsKey(originalUrl)) {
            // Возвращаем существующую ссылку
            ShortLink existingLink = userMap.get(originalUrl);
            return config.getBaseUrl() + existingLink.getShortCode();
        }

        // Генерируем уникальный код для этого пользователя и URL
        String shortCode = generateUniqueCode(userNickname, originalUrl);

        // Вычисляем время истечения
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(config.getDefaultTtlHours());

        // Создаем короткую ссылку
        ShortLink shortLink = new ShortLink(shortCode, originalUrl, userNickname, clickLimit, expiresAt);

        // Сохраняем связь URL -> ShortLink для этого пользователя
        userMap.put(originalUrl, shortLink);

        // Сохраняем изменения в файл
        saveUserLinks(userLinks);

        return config.getBaseUrl() + shortCode;
    }

    // Получает оригинальный URL по короткому коду и увеличивает счетчик кликов
    public String getOriginalUrl(String shortCode) {
        // Загружаем данные из файла
        Map<String, Map<String, ShortLink>> userLinks = loadUserLinks();
        ShortLink link = findLinkByCode(shortCode, userLinks);

        if (link == null) {
            return null;
        }

        // Проверяем, не истекла ли ссылка
        if (link.isExpired()) {
            link.setActive(false);
            saveUserLinks(userLinks); // Сохраняем изменение статуса
            return null;
        }

        // Проверяем лимит кликов
        if (link.isClickLimitExceeded()) {
            return null;
        }

        // Увеличиваем счетчик кликов
        link.incrementClickCount();

        // Если лимит исчерпан после этого клика, деактивируем ссылку
        if (link.isClickLimitExceeded()) {
            link.setActive(false);
        }

        // Сохраняем изменения (обновленный счетчик кликов)
        saveUserLinks(userLinks);

        return link.getOriginalUrl();
    }

    // Находит ссылку по коду во всех пользователях
    private ShortLink findLinkByCode(String shortCode, Map<String, Map<String, ShortLink>> userLinks) {
        for (Map<String, ShortLink> userMap : userLinks.values()) {
            for (ShortLink link : userMap.values()) {
                if (link.getShortCode().equals(shortCode)) {
                    return link;
                }
            }
        }
        return null;
    }

    // Находит ссылку по коду (загружает данные из файла)
    private ShortLink findLinkByCode(String shortCode) {
        Map<String, Map<String, ShortLink>> userLinks = loadUserLinks();
        return findLinkByCode(shortCode, userLinks);
    }

    // Получает информацию о ссылке
    public ShortLink getLinkInfo(String shortCode) {
        return findLinkByCode(shortCode);
    }

    // Получает все ссылки пользователя по никнейму
    public List<ShortLink> getUserLinks(String userNickname) {
        Map<String, Map<String, ShortLink>> userLinks = loadUserLinks();
        Map<String, ShortLink> userMap = userLinks.getOrDefault(userNickname, Collections.emptyMap());
        return new ArrayList<>(userMap.values());
    }

    // Удаляет ссылку (только если пользователь является её создателем)
    public boolean deleteLink(String shortCode, String userNickname) {
        // Загружаем данные из файла
        Map<String, Map<String, ShortLink>> userLinks = loadUserLinks();
        Map<String, ShortLink> userMap = userLinks.get(userNickname);
        if (userMap == null) {
            return false;
        }

        // Находим ссылку по коду
        String urlToRemove = null;
        for (Map.Entry<String, ShortLink> entry : userMap.entrySet()) {
            if (entry.getValue().getShortCode().equals(shortCode)) {
                if (!entry.getValue().getUserNickname().equals(userNickname)) {
                    return false; // Пользователь не является создателем ссылки
                }
                urlToRemove = entry.getKey();
                break;
            }
        }

        if (urlToRemove == null) {
            return false;
        }

        // Удаляем из индекса пользователя
        userMap.remove(urlToRemove);

        // Если у пользователя больше нет ссылок, можно удалить его запись
        if (userMap.isEmpty()) {
            userLinks.remove(userNickname);
        }

        // Сохраняем изменения в файл
        saveUserLinks(userLinks);

        return true;
    }

    // Удаляет истекшие ссылки
    public void removeExpiredLinks() {
        // Загружаем данные из файла
        Map<String, Map<String, ShortLink>> userLinks = loadUserLinks();
        boolean hasChanges = false;

        for (Map.Entry<String, Map<String, ShortLink>> userEntry : userLinks.entrySet()) {
            Map<String, ShortLink> userMap = userEntry.getValue();
            List<String> urlsToRemove = new ArrayList<>();

            for (Map.Entry<String, ShortLink> linkEntry : userMap.entrySet()) {
                if (linkEntry.getValue().isExpired()) {
                    urlsToRemove.add(linkEntry.getKey());
                }
            }

            for (String url : urlsToRemove) {
                userMap.remove(url);
                hasChanges = true;
            }

            // Если у пользователя больше нет ссылок, удаляем его запись
            if (userMap.isEmpty()) {
                userLinks.remove(userEntry.getKey());
            }
        }

        if (hasChanges) {
            // Сохраняем изменения после удаления истекших ссылок
            saveUserLinks(userLinks);
        }
    }

    // Получает статистику по ссылке
    public String getLinkStatus(String shortCode) {
        ShortLink link = findLinkByCode(shortCode);
        if (link == null) {
            return "Ссылка не найдена";
        }

        if (link.isExpired()) {
            return "Ссылка истекла";
        }

        if (link.isClickLimitExceeded()) {
            return "Лимит переходов исчерпан";
        }

        return String.format("Активна. Кликов: %d/%s",
                link.getClickCount(),
                link.getClickLimit() != null ? link.getClickLimit().toString() : "без ограничений");
    }
}
