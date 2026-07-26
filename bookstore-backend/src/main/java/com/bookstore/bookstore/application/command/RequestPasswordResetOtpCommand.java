package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record RequestPasswordResetOtpCommand(
        String email,
        AuthRequestMetadata metadata
) {
    public RequestPasswordResetOtpCommand(String email) {
        this(email, AuthRequestMetadata.empty());
    }

    public RequestPasswordResetOtpCommand {
        if (email == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "email");
        }
        metadata = metadata == null ? AuthRequestMetadata.empty() : metadata;
    }
}
