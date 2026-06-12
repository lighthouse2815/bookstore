package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record ResetPasswordCommand(
        String resetToken,
        String newPassword
) {
    public ResetPasswordCommand {
        if (resetToken == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "resetToken");
        }

        if (newPassword == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_AUTH_PASSWORD, "newPassword");
        }
    }
}
