package com.urlshortener.service;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Адаптер Gson для сериализации/десериализации LocalDateTime
 * Предоставляет единый форматтер для использования в приложении
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    // Простой формат для вывода: yyyy-MM-dd HH:mm
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            // Сохраняем в ISO формате для совместимости
            out.value(value.format(ISO_FORMATTER));
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String dateTimeString = in.nextString();
        // Пробуем сначала ISO формат, потом простой формат
        try {
            return LocalDateTime.parse(dateTimeString, ISO_FORMATTER);
        } catch (Exception e) {
            return LocalDateTime.parse(dateTimeString, FORMATTER);
        }
    }
}
