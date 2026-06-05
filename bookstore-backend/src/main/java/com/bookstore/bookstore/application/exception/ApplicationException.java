package com.bookstore.bookstore.application.exception;

import java.util.Objects;

public class ApplicationException extends RuntimeException {

    private final ApplicationErrorCode errorCode;

    public ApplicationException(ApplicationErrorCode errorCode, Object... args) {
        super(Objects.requireNonNull(errorCode, "errorCode").message(args));
        this.errorCode = errorCode;
    }

    public ApplicationErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}
