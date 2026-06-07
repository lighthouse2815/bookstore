package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.Gender;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        UUID userId,
        String lastName,
        String firstName,
        String avatarUrl,
        Gender gender,
        LocalDate dateOfBirth,
        Instant createdAt,
        Instant updatedAt
) {
}
