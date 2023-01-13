package com.coditory.quark.uri;

import java.util.Locale;

import static com.coditory.quark.uri.Preconditions.expectNonNull;

final class Strings {
    private Strings() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static boolean isNullOrEmpty(String text) {
        return text == null || text.isEmpty();
    }

    static boolean isNotNullOrEmpty(String text) {
        return !isNullOrEmpty(text);
    }

    static boolean isNullOrBlank(String text) {
        return text == null || text.isBlank();
    }

    static boolean isNotNullOrBlank(String text) {
        return !isNullOrBlank(text);
    }

    static String emptyToNull(String text) {
        return text == null || text.isEmpty()
                ? null
                : text;
    }

    static String blankToNull(String text) {
        return text == null || text.isBlank()
                ? null
                : text;
    }

    static String lowerCase(String text) {
        expectNonNull(text, "text");
        return text.toLowerCase(Locale.ROOT);
    }

    public static boolean containsWhitespace(String text) {
        expectNonNull(text, "text");
        return text.codePoints().anyMatch(Character::isWhitespace);
    }
}
