package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record DeleteBookshelfCommand(
        UUID shelfId,
        UUID userId
) {
}
