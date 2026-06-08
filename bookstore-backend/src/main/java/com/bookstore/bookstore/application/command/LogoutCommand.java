package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record LogoutCommand(
        String refreshToken
) {
    public LogoutCommand {
        if (refreshToken == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "refreshToken");
        }
    }
}
