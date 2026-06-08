package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.UserStatus;
import java.util.Set;
import java.util.UUID;

public record LoginResponse(
        UUID userId,
        UserStatus status,
        Set<String> roles,
        String accessToken,
        String refreshToken
) {
}
