package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public record GoogleLoginCommand(
        String idToken
) {
    public GoogleLoginCommand {
        if (idToken == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "idToken");
        }
    }
}
