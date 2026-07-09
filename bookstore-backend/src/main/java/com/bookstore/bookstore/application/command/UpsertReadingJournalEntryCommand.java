package com.bookstore.bookstore.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpsertReadingJournalEntryCommand(
        UUID userId,
        UUID bookId,
        LocalDate entryDate,
        String note,
        Integer currentPage,
        BigDecimal progressPercent
) {
}
