package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record LogoutCommand(
        String refreshToken,
        AuthRequestMetadata metadata
) {
    public LogoutCommand(String refreshToken) {
        this(refreshToken, AuthRequestMetadata.empty());
    }

    public LogoutCommand {
        if (refreshToken == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "refreshToken");
        }
        metadata = metadata == null ? AuthRequestMetadata.empty() : metadata;
    }
}
