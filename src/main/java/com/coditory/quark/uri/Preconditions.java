package com.coditory.quark.uri;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class Preconditions {
    private Preconditions() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static void expect(boolean check, String message, Object... args) {
        if (!check) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    static <T> T expectNonNull(@Nullable T value, String name) {
        if (value == null) {
            String message = message("Expected non-null value", name);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static String expectNonEmpty(@Nullable String value, String name) {
        if (value == null || value.isEmpty()) {
            String message = message("Expected non-null and non-empty value", name, value);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static String expectNonBlank(@Nullable String value, String name) {
        if (value == null || value.isBlank()) {
            String message = message("Expected non-null and non-blank value", name, value);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    static String expectNoWhitespaces(@Nullable String value, String name) {
        if (value == null || Strings.containsWhitespace(value)) {
            String message = message("Expected non-null value with no whitespaces", name, value);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String message(String expectation, String fieldName, Object value) {
        String field = fieldName != null ? (": " + fieldName) : "";
        String stringValue = value instanceof String
                ? ("\"" + value + "\"")
                : Objects.toString(value);
        return expectation + field + ". Got: " + stringValue;
    }

    private static String message(String expectation, String fieldName) {
        String field = fieldName != null ? (": " + fieldName) : "";
        return expectation + field;
    }
}
