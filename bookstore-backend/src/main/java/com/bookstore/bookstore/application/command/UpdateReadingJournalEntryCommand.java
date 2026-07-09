package com.bookstore.bookstore.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateReadingJournalEntryCommand(
        UUID entryId,
        UUID userId,
        String note,
        Integer currentPage,
        BigDecimal progressPercent
) {
}
