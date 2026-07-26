package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.UUID;

public record BookshelfSummaryResult(
        UUID id,
        String name,
        long bookCount,
        Instant createdAt,
        Instant updatedAt
) {
}
