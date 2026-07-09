package com.bookstore.bookstore.application.command;

import java.util.List;
import java.util.UUID;

public record ReorderBookshelfItemsCommand(
        UUID shelfId,
        UUID userId,
        List<UUID> itemIds
) {
    public ReorderBookshelfItemsCommand {
        itemIds = itemIds == null ? List.of() : List.copyOf(itemIds);
    }
}
