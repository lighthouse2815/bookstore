package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID cartId,
        UUID userId,
        List<CartItemResponse> items,
        int totalQuantity,
        BigDecimal totalAmount
) {
}
