package com.urlshortener;

import com.urlshortener.config.Config;
import com.urlshortener.model.ShortLink;
import com.urlshortener.service.ActiveUsersService;
import com.urlshortener.service.TTLService;
import com.urlshortener.service.UrlShortenerService;

import java.awt.Desktop;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static UrlShortenerService urlShortenerService;
    private static TTLService expirationService;
    private static ActiveUsersService activeUsersService;
    private static String currentUserNickname = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Обрабатываем аргументы командной строки для пути к конфигу
        String configPath = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--config") || args[i].equals("-c")) {
                if (i + 1 < args.length) {
                    configPath = args[i + 1];
                } else {
                    System.err.println("Ошибка: путь к конфигурационному файлу не указан");
                    System.exit(1);
                }
            }
        }

        // Устанавливаем путь к конфигу, если указан
        if (configPath != null) {
            Config.setConfigPath(configPath);
        }

        // Инициализируем сервисы после загрузки конфига
        urlShortenerService = new UrlShortenerService();
        expirationService = new TTLService(urlShortenerService);
        activeUsersService = new ActiveUsersService();

        System.out.print("Введите ваш никнейм: ");
        currentUserNickname = scanner.nextLine().trim();

        if (currentUserNickname.isEmpty()) {
            System.err.println("Ошибка: никнейм не может быть пустым!");
            System.exit(1);
        }

        // Увеличиваем счетчик активных пользователей
        activeUsersService.incrementActiveUsers();

        // Добавляем shutdown hook для уменьшения счетчика при завершении
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            activeUsersService.decrementActiveUsers();
        }));

        System.out.println("Добро пожаловать, " + currentUserNickname + "!");
        System.out.println();

        // Запускаем сервис автоматической очистки
        expirationService.start();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        createShortLink();
                        break;
                    case "2":
                        openShortLink();
                        break;
                    case "3":
                        viewMyLinks();
                        break;
                    case "4":
                        deleteLink();
                        break;
                    case "5":
                        showUserTty();
                        break;
                    case "0":
                        running = false;
                        System.out.println("Завершение работы...");
                        break;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }

            System.out.println();
        }

        expirationService.stop();
        scanner.close();

        // Уменьшаем счетчик активных пользователей
        activeUsersService.decrementActiveUsers();
    }

    private static void printMenu() {
        System.out.println("Выберите действие:");
        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Просмотреть мои ссылки");
        System.out.println("4. Удалить ссылку");
        System.out.println("5. Показать мой никнейм");
        System.out.println("0. Выход");
        System.out.print("Ваш выбор: ");
    }

    private static void createShortLink() {
        System.out.print("Введите длинный URL: ");
        String originalUrl = scanner.nextLine().trim();

        if (originalUrl.isEmpty()) {
            System.out.println("URL не может быть пустым!");
            return;
        }

        System.out.print("Введите лимит переходов (или нажмите Enter для безлимита): ");
        String limitInput = scanner.nextLine().trim();
        Integer clickLimit = null;

        if (!limitInput.isEmpty()) {
            try {
                clickLimit = Integer.parseInt(limitInput);
                if (clickLimit <= 0) {
                    System.out.println("Лимит должен быть положительным числом!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат числа!");
                return;
            }
        }

        String shortUrl = urlShortenerService.createShortLink(originalUrl, currentUserNickname, clickLimit);
        System.out.println("Короткая ссылка создана: " + shortUrl);
        System.out.println("Время жизни ссылки: 24 часа");
    }

    private static void openShortLink() {
        System.out.print("Введите короткий код ссылки (например, 3DZHeG): ");
        String shortCode = scanner.nextLine().trim();

        if (shortCode.isEmpty()) {
            System.out.println("Код не может быть пустым!");
            return;
        }

        // Убираем префикс BASE_URL, если пользователь ввел полный URL
        if (shortCode.contains("/")) {
            shortCode = shortCode.substring(shortCode.lastIndexOf("/") + 1);
        }

        String originalUrl = urlShortenerService.getOriginalUrl(shortCode);

        if (originalUrl == null) {
            ShortLink link = urlShortenerService.getLinkInfo(shortCode);
            if (link == null) {
                System.out.println("Ссылка не найдена!");
            } else if (link.isExpired()) {
                System.out.println("Ссылка истекла! Время жизни ссылки составляет 24 часа.");
            } else if (link.isClickLimitExceeded()) {
                System.out.println(
                        "Лимит переходов исчерпан! Максимальное количество переходов: " + link.getClickLimit());
            }
            return;
        }

        System.out.println("Переход по ссылке: " + originalUrl);

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(originalUrl));
                System.out.println("Ссылка открыта в браузере");
            } else {
                System.out.println("Не удалось открыть браузер автоматически. Скопируйте ссылку: " + originalUrl);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при открытии браузера: " + e.getMessage());
            System.out.println("Скопируйте ссылку вручную: " + originalUrl);
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static void viewMyLinks() {
        List<ShortLink> links = urlShortenerService.getUserLinks(currentUserNickname);

        if (links.isEmpty()) {
            System.out.println("У вас пока нет созданных ссылок.");
            return;
        }

        System.out.println("Ваши ссылки:");
        System.out.println("─────────────────────────────────────────────────────────────");
        for (ShortLink link : links) {
            String status = link.canBeAccessed() ? "Активна" : "Неактивна";
            if (link.isExpired()) {
                status = "Истекла";
            } else if (link.isClickLimitExceeded()) {
                status = "Лимит исчерпан";
            }

            System.out.println("Код: " + link.getShortCode());
            System.out.println("  URL: " + link.getOriginalUrl());
            System.out.println("  Короткая ссылка: clck.ru/" + link.getShortCode());
            System.out.println("  Кликов: " + link.getClickCount() +
                    (link.getClickLimit() != null ? "/" + link.getClickLimit() : "/без ограничений"));
            System.out.println("  Статус: " + status);
            System.out.println("  Создана: " + link.getCreatedAt().format(DATE_FORMATTER));
            System.out.println("  Истекает: " + link.getExpiresAt().format(DATE_FORMATTER));
            System.out.println("─────────────────────────────────────────────────────────────");
        }
    }

    private static void deleteLink() {
        System.out.print("Введите короткий код ссылки для удаления: ");
        String shortCode = scanner.nextLine().trim();

        if (shortCode.contains("/")) {
            shortCode = shortCode.substring(shortCode.lastIndexOf("/") + 1);
        }

        boolean deleted = urlShortenerService.deleteLink(shortCode, currentUserNickname);

        if (deleted) {
            System.out.println("Ссылка успешно удалена");
        } else {
            ShortLink link = urlShortenerService.getLinkInfo(shortCode);
            if (link == null) {
                System.out.println("Ссылка не найдена!");
            } else {
                System.out.println("Вы не можете удалить эту ссылку. Вы не являетесь её создателем.");
            }
        }
    }

    private static void showUserTty() {
        System.out.println("Ваш никнейм: " + currentUserNickname);
        System.out.println("Этот никнейм используется для идентификации всех ваших ссылок.");
    }
}
