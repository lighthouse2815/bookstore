package com.bookstore.bookstore.application.validation;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;

public final class ApplicationGuard {

    private ApplicationGuard() {
    }

    public static int requireNonNegative(
            int value,
            String argumentName
    ) {
        if (value < 0) {
            throw new ApplicationException(
                    ApplicationErrorCode.INVALID_ARGUMENT,
                    argumentName
            );
        }

        return value;
    }
}