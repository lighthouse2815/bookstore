package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record DeleteAuthorCommand(
        UUID authorId
) {
    public DeleteAuthorCommand {
        if (authorId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "authorId");
        }
    }
}
