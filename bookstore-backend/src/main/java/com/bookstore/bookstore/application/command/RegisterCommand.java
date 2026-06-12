package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record RegisterCommand(
        String email,
        String password
) {
    public RegisterCommand {
        if (password == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_AUTH_PASSWORD);
        }
    }
}
