package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.domain.enums.OutboxStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutboxEventTest {
    @Test
    void failure_usesBoundedExponentialBackoffThenDeadLetters() {
        OutboxEvent event = event();
        Instant now = Instant.EPOCH;
        event.claim("worker-1", now);
        event.fail("network", 2, now);
        assertEquals(OutboxStatus.FAILED, event.getStatus());
        assertTrue(event.getNextAttemptAt().isAfter(now));
        event.claim("worker-1", event.getNextAttemptAt());
        event.fail("network", 2, event.getNextAttemptAt());
        assertEquals(OutboxStatus.DEAD, event.getStatus());
    }

    @Test
    void staleProcessingEvent_canBeReclaimed() {
        OutboxEvent event = event();
        event.claim("worker-1", Instant.EPOCH);
        event.reclaim(Instant.EPOCH.plusSeconds(60));
        assertEquals(OutboxStatus.PENDING, event.getStatus());
        assertEquals(null, event.getLockedAt());
    }

    @Test
    void retry_deadEvent_returnsItToPending() {
        OutboxEvent event = event();
        event.claim("worker-1", Instant.EPOCH);
        event.fail("network", 1, Instant.EPOCH);
        event.retry(Instant.EPOCH.plusSeconds(10));
        assertEquals(OutboxStatus.PENDING, event.getStatus());
    }

    private static OutboxEvent event() {
        Instant now = Instant.EPOCH;
        return new OutboxEvent(UUID.randomUUID(), "ORDER", UUID.randomUUID(), "ORDER_CREATED", "{}", "a".repeat(64), OutboxStatus.PENDING, 0, now, null, null, null, now, null, now, 0);
    }
}
