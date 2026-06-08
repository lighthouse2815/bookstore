package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ImportReceiptResponse(
        UUID id,
        UUID supplierId,
        List<ImportReceiptItemResponse> items,
        BigDecimal totalAmount,
        String note,
        Instant createdAt,
        Instant updatedAt,
        UUID createdBy
) {
}
