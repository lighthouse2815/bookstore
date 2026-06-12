package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record RequestRegistrationOtpCommand(
        String email
) {
    public RequestRegistrationOtpCommand {
        if (email == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "email");
        }
    }
}
