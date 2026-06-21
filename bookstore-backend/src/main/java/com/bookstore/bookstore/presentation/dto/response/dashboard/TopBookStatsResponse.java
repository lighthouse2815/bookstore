package com.bookstore.bookstore.presentation.dto.response.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record TopBookStatsResponse(
        UUID bookId,
        String title,
        long soldQuantity,
        BigDecimal revenue
) {
}
