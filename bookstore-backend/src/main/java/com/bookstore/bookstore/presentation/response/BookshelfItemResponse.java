package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record BookshelfItemResponse(
        UUID id,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt,
        BookResponse book
) {
}
