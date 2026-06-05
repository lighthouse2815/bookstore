package com.bookstore.bookstore.shared.util;

public final class StringUtils {

    private StringUtils() {}

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
