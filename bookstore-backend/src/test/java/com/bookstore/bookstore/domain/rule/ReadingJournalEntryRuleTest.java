package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReadingJournalEntryRuleTest {

    @Test
    void requireValidProgressPercent_rejectsValueOverOneHundred() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> ReadingJournalEntryRule.requireValidProgressPercent(new BigDecimal("100.01"))
        );

        assertEquals(
                DomainErrorCode.INVALID_READING_JOURNAL_ENTRY_PROGRESS_PERCENT,
                exception.getErrorCode()
        );
    }
}
