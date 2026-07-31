package com.bookstore.bookstore.domain.enums;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.util.Arrays;
import java.util.Locale;

public enum CategoryLocale {
    VI("vi"),
    EN("en");

    private final String code;

    CategoryLocale(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CategoryLocale fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException(DomainErrorCode.INVALID_CATEGORY_LOCALE, "locale");
        }

        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(locale -> locale.code.equals(normalizedCode))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                        DomainErrorCode.INVALID_CATEGORY_LOCALE,
                        "locale"
                ));
    }
}
