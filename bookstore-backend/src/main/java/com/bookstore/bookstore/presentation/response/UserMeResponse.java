package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserMeResponse(
        UUID userId,
        String username,
        String email,
        String phoneNumber,
        UserStatus status,
        boolean locked,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
