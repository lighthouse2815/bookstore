package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record RefreshAccessTokenCommand(
        String refreshToken,
        AuthRequestMetadata metadata
) {
    public RefreshAccessTokenCommand(String refreshToken) {
        this(refreshToken, AuthRequestMetadata.empty());
    }

    public RefreshAccessTokenCommand {
        if (refreshToken == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "refreshToken");
        }
        metadata = metadata == null ? AuthRequestMetadata.empty() : metadata;
    }
}
