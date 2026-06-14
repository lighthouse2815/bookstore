package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record BookImageResponse(
        UUID id,
        UUID bookId,
        String imageUrl,
        Boolean primaryImage,
        Integer sortOrder,
        String altText,
        Instant createdAt
) {
}
