package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResult(
        UUID cartId,
        UUID userId,
        List<CartItemResult> items,
        int totalQuantity,
        BigDecimal totalAmount
) {
    public CartResult {
        items = items == null ? List.of() : List.copyOf(items);
        totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
    }
}
