package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String imageUrl,
        UUID categoryId,
        UUID authorId,
        UUID publisherId,
        Instant createdAt,
        Instant updatedAt
) {
}
