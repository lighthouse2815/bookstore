package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        UUID parentId,
        Instant createdAt,
        Instant updatedAt
) {
}
