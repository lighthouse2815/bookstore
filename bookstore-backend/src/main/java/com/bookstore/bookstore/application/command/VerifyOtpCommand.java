package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record VerifyOtpCommand(
        String email,
        String otpCode
) {
    public VerifyOtpCommand {
        if (email == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "email");
        }

        if (otpCode == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "otpCode");
        }
    }
}
