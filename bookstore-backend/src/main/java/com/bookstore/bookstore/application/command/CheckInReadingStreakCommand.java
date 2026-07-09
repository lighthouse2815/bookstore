package com.bookstore.bookstore.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckInReadingStreakCommand(
        UUID userId,
        UUID bookId,
        String note,
        Integer currentPage,
        BigDecimal progressPercent
) {
}
