package com.bookstore.bookstore.application.result;

import java.time.Instant;

public record PasswordResetTokenResult(
        String resetToken,
        Instant expiresAt
) {
}
