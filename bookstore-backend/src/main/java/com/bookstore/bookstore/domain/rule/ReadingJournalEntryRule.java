package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class ReadingJournalEntryRule {

    private ReadingJournalEntryRule() {
    }

    public static void requireActive(boolean deleted) {
        if (deleted) {
            throw new DomainException(DomainErrorCode.READING_JOURNAL_ENTRY_ALREADY_DELETED);
        }
    }

    public static void requireDeleted(boolean deleted) {
        if (!deleted) {
            throw new DomainException(DomainErrorCode.READING_JOURNAL_ENTRY_ALREADY_ACTIVE);
        }
    }

    public static void requireNotFutureDate(LocalDate entryDate, LocalDate today) {
        if (entryDate.isAfter(today)) {
            throw new DomainException(DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_DATE, "entryDate");
        }
    }

    public static void requireNonNegativeCurrentPage(Integer currentPage) {
        if (currentPage != null && currentPage < 0) {
            throw new DomainException(DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_CURRENT_PAGE, "currentPage");
        }
    }

    public static void requireValidProgressPercent(BigDecimal progressPercent) {
        if (progressPercent != null
                && (progressPercent.compareTo(BigDecimal.ZERO) < 0
                || progressPercent.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new DomainException(
                    DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_PROGRESS_PERCENT,
                    "progressPercent"
            );
        }
    }
}
