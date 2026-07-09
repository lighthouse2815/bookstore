package com.bookstore.bookstore.application.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReadingJournalEntryResult(
        UUID id,
        LocalDate entryDate,
        String note,
        Integer currentPage,
        BigDecimal progressPercent,
        Instant createdAt,
        Instant updatedAt,
        BookQueryResult book
) {
}
