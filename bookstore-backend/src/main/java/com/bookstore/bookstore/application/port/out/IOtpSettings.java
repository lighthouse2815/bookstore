package com.bookstore.bookstore.application.port.out;

public interface IOtpSettings {

    long expirationMinutes();

    long resendCooldownSeconds();

    long resendMaxRequestsPerWindow();

    long resendWindowMinutes();

    default long maxAttempts() {
        return 5;
    }
}
