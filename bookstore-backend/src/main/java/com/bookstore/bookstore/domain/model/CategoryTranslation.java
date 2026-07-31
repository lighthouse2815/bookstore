package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.CategoryLocale;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;

public record CategoryTranslation(
        CategoryLocale locale,
        String name,
        String description
) {
    public CategoryTranslation {
        locale = Guard.notNull(locale, DomainErrorCode.INVALID_CATEGORY_LOCALE, "locale");
        name = Guard.notBlank(name, DomainErrorCode.INVALID_CATEGORY_TRANSLATION, "name");
    }
}
