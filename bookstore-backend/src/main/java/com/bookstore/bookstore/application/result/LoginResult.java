package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.UserStatus;
import java.util.Set;
import java.util.UUID;

public record LoginResult(
        UUID userId,
        UserStatus status,
        Set<String> roles,
        String accessToken,
        String refreshToken
) {
    public LoginResult {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
