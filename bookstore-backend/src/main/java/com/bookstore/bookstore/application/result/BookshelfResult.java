package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookshelfResult(
        UUID id,
        String name,
        long bookCount,
        List<BookshelfItemResult> items,
        Instant createdAt,
        Instant updatedAt
) {
    public BookshelfResult {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
