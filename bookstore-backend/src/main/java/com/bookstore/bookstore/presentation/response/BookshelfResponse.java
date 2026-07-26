package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookshelfResponse(
        UUID id,
        String name,
        long bookCount,
        List<BookshelfItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
