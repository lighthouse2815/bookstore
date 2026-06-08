package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID bookId,
        String bookTitle,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
