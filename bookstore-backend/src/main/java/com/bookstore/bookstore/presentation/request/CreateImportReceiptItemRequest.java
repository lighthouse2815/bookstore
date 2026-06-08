package com.bookstore.bookstore.presentation.request;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateImportReceiptItemRequest(
        UUID bookId,
        BigDecimal unitCost,
        Integer quantity
) {
}
