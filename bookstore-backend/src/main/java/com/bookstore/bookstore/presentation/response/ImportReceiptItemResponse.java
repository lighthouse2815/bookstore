package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ImportReceiptItemResponse(
        UUID id,
        UUID bookId,
        String bookTitle,
        BigDecimal unitCost,
        Integer quantity,
        BigDecimal lineTotal
) {
}
