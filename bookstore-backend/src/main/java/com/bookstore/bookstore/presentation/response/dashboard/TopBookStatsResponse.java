package com.bookstore.bookstore.presentation.response.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record TopBookStatsResponse(
        UUID bookId,
        String title,
        long soldQuantity,
        BigDecimal revenue
) {
}
