package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record BookshelfSummaryResponse(
        UUID id,
        String name,
        long bookCount,
        Instant createdAt,
        Instant updatedAt
) {
}
