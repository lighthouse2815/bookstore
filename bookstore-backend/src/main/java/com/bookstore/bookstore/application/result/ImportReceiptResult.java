package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ImportReceiptResult(
        UUID id,
        UUID supplierId,
        List<ImportReceiptItemResult> items,
        BigDecimal totalAmount,
        String note,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy
) {
    public ImportReceiptResult {
        items = items == null ? List.of() : List.copyOf(items);
        totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }
}
