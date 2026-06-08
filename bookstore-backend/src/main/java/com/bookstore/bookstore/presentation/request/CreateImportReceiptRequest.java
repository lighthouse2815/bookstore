package com.bookstore.bookstore.presentation.request;

import java.util.List;
import java.util.UUID;

public record CreateImportReceiptRequest(
        UUID supplierId,
        List<CreateImportReceiptItemRequest> items,
        String note
) {
}
