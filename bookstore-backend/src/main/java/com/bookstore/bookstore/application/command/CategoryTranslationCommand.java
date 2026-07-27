package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.CategoryLocale;

public record CategoryTranslationCommand(
        CategoryLocale locale,
        String name,
        String description
) {
    public CategoryTranslationCommand {
        if (locale == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "locale");
        }
        if (name == null || name.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "name");
        }
    }
}
