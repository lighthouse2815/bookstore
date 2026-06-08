package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.StockMovementType;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
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
