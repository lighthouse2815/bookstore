package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record AuthorResponse(
        UUID id,
        String name,
        String biography,
        String avatarUrl,
        Integer birthYear,
        Integer deathYear,
        Instant createdAt,
        Instant updatedAt
) {
}
