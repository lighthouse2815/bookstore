package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record UpdateUserCommand(
        UUID userId,
        String username,
        String phoneNumber,
        String email
) {
    public UpdateUserCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
    }
}
