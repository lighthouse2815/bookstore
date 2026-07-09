package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record UpdateBookshelfCommand(
        UUID shelfId,
        UUID userId,
        String name
) {
}
