package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record AddBookshelfItemCommand(
        UUID shelfId,
        UUID userId,
        UUID bookId
) {
}
