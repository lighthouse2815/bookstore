package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;
import java.util.List;

public record CreateCategoryCommand(
        String code,
        List<CategoryTranslationCommand> translations,
        UUID parentId,
        UUID imageFileAssetId
) {
    public CreateCategoryCommand {
        if (code == null || code.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "code");
        }
        if (translations == null || translations.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "translations");
        }
    }
}
