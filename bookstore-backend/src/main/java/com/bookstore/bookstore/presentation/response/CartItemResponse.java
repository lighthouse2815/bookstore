package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID bookId,
        String bookTitle,
        String imageUrl,
        BigDecimal price,
        int quantity,
        BigDecimal lineTotal
) {
}
