package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.StockMovementType;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResult(
        UUID id,
        UUID bookId,
        StockMovementType type,
        Integer quantity,
        Integer beforeQuantity,
        Integer afterQuantity,
        UUID referenceId,
        String referenceType,
        String note,
        Instant createdAt,
        UUID createdBy
) {
}
