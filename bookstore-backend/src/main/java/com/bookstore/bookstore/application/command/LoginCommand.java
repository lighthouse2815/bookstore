package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record LoginCommand(
        String username,
        String password
) {
    public LoginCommand {
        if (username == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "username");
        }

        if (password == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_AUTH_PASSWORD);
        }
    }
}
