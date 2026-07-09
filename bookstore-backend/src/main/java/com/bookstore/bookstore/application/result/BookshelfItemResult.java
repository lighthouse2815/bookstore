package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.UUID;

public record BookshelfItemResult(
        UUID id,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt,
        BookQueryResult book
) {
}
