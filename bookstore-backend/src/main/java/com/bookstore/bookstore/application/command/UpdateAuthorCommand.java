package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record UpdateAuthorCommand(
        UUID authorId,
        String name,
        String biography
) {
    public UpdateAuthorCommand {
        if (authorId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "authorId");
        }

        if (name == null || name.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "name");
        }
    }
}
