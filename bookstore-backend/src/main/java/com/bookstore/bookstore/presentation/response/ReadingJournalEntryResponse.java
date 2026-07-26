package com.bookstore.bookstore.presentation.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReadingJournalEntryResponse(
        UUID id,
        LocalDate entryDate,
        String note,
        Integer currentPage,
        BigDecimal progressPercent,
        Instant createdAt,
        Instant updatedAt,
        BookResponse book
) {
}
