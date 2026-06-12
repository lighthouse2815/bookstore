package com.bookstore.bookstore.application.exception;

public class GoogleIdTokenVerificationException extends RuntimeException {

    public GoogleIdTokenVerificationException(String message) {
        super(message);
    }

    public GoogleIdTokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
