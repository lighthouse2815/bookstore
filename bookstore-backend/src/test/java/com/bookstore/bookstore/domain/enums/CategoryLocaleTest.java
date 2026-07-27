package com.bookstore.bookstore.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class CategoryLocaleTest {

    @Test
    void fromCode_normalizesSupportedCode() {
        assertEquals(CategoryLocale.VI, CategoryLocale.fromCode(" VI "));
        assertEquals(CategoryLocale.EN, CategoryLocale.fromCode("en"));
    }

    @Test
    void fromCode_whenCodeIsUnsupported_rejects() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> CategoryLocale.fromCode("fr")
        );

        assertEquals(DomainErrorCode.INVALID_CATEGORY_LOCALE, exception.getErrorCode());
    }
}
