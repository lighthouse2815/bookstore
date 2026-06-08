package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
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
