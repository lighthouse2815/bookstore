package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;

public record CategoryTranslation(
        String locale,
        String name,
        String description
) {
    public CategoryTranslation {
        locale = Guard.notBlank(locale, DomainErrorCode.INVALID_CATEGORY_TRANSLATION, "locale");
        name = Guard.notBlank(name, DomainErrorCode.INVALID_CATEGORY_TRANSLATION, "name");
    }
}
