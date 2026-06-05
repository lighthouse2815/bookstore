package com.bookstore.bookstore.domain.exception;

import java.util.Objects;

public class DomainException extends RuntimeException {

    private final DomainErrorCode errorCode;

    public DomainException(DomainErrorCode errorCode, Object... args) {
        super(Objects.requireNonNull(errorCode, "errorCode").message(args));
        this.errorCode = errorCode;
    }

    public DomainErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }
}
