package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OutboxEventRuleTest {

    @Test
    void requireClaimable_rejectsEventScheduledForLater() {
        Instant now = Instant.EPOCH;
        DomainException exception = assertThrows(
                DomainException.class,
                () -> OutboxEventRule.requireClaimable(
                        OutboxStatus.PENDING,
                        now.plusSeconds(1),
                        now
                )
        );

        assertEquals(DomainErrorCode.OUTBOX_EVENT_NOT_CLAIMABLE, exception.getErrorCode());
    }

    @Test
    void requireProcessing_rejectsPendingEvent() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> OutboxEventRule.requireProcessing(OutboxStatus.PENDING)
        );

        assertEquals(DomainErrorCode.OUTBOX_EVENT_NOT_PROCESSING, exception.getErrorCode());
    }
}
