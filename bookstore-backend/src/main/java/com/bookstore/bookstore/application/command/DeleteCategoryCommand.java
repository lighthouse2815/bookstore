package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record DeleteCategoryCommand(
        UUID categoryId
) {
    public DeleteCategoryCommand {
        if (categoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "categoryId");
        }
    }
}
