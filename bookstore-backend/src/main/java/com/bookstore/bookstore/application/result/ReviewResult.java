package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.UUID;

public record ReviewResult(
        UUID reviewId,
        UUID userId,
        UUID bookId,
        UUID orderItemId,
        int rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}
