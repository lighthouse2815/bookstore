package com.bookstore.bookstore.presentation.response;

import java.time.Instant;

public record PasswordResetTokenResponse(
        String resetToken,
        Instant expiresAt
) {
}
