package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class OutboxEventRule {

    private OutboxEventRule() {
    }

    public static void requireNonNegativeAttemptCount(int attemptCount) {
        if (attemptCount < 0) {
            throw new DomainException(DomainErrorCode.INVALID_OUTBOX_ATTEMPT_COUNT, "attemptCount");
        }
    }

    public static void requireClaimable(OutboxStatus status, Instant nextAttemptAt, Instant now) {
        if ((status != OutboxStatus.PENDING && status != OutboxStatus.FAILED)
                || nextAttemptAt.isAfter(now)) {
            throw new DomainException(DomainErrorCode.OUTBOX_EVENT_NOT_CLAIMABLE);
        }
    }

    public static void requireProcessing(OutboxStatus status) {
        if (status != OutboxStatus.PROCESSING) {
            throw new DomainException(DomainErrorCode.OUTBOX_EVENT_NOT_PROCESSING);
        }
    }

    public static void requirePositiveMaxAttempts(int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_OUTBOX_MAX_ATTEMPTS, "maxAttempts");
        }
    }

    public static void requireRetryable(OutboxStatus status) {
        if (status != OutboxStatus.DEAD && status != OutboxStatus.FAILED) {
            throw new DomainException(DomainErrorCode.OUTBOX_EVENT_NOT_RETRYABLE);
        }
    }
}
