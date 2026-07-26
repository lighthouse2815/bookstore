package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record LoginCommand(
        String username,
        String password,
        AuthRequestMetadata metadata
) {
    public LoginCommand(String username, String password) {
        this(username, password, AuthRequestMetadata.empty());
    }

    public LoginCommand {
        if (username == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "username");
        }

        if (password == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_AUTH_PASSWORD);
        }
        metadata = metadata == null ? AuthRequestMetadata.empty() : metadata;
    }
}
