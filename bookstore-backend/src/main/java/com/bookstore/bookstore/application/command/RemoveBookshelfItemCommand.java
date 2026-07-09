package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record RemoveBookshelfItemCommand(
        UUID shelfId,
        UUID userId,
        UUID bookId
) {
}
