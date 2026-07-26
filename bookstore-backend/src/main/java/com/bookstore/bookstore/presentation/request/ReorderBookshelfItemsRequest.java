package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ReorderBookshelfItemsRequest(
        @NotEmpty(message = "itemIds khong duoc de trong")
        List<@NotNull(message = "itemId khong duoc null") UUID> itemIds
) {
}
