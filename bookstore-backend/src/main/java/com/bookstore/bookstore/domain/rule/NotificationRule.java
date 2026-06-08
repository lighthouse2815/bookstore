package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;

public final class NotificationRule {

    private NotificationRule() {
    }

    public static void requireReadStateConsistent(boolean read, Instant readAt) {
        if (read && readAt == null) {
            throw new DomainException(DomainErrorCode.INVALID_NOTIFICATION_READ_AT, "readAt");
        }

        if (!read && readAt != null) {
            throw new DomainException(DomainErrorCode.INVALID_NOTIFICATION_READ_AT, "readAt");
        }
    }

    public static void requireAuditTimeline(
            Instant createdAt,
            Instant updatedAt,
            Instant readAt,
            Instant deletedAt
    ) {
        Guard.notBefore(readAt, createdAt, DomainErrorCode.INVALID_NOTIFICATION_AUDIT_ORDER, "readAt", "createdAt");
        Guard.notBefore(updatedAt, readAt, DomainErrorCode.INVALID_NOTIFICATION_AUDIT_ORDER, "updatedAt", "readAt");
        Guard.notBefore(deletedAt, readAt, DomainErrorCode.INVALID_NOTIFICATION_AUDIT_ORDER, "deletedAt", "readAt");
    }

    public static void requireCanMarkRead(Instant deletedAt, boolean read) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.DELETED_NOTIFICATION_CANNOT_MARK_READ);
        }

        if (read) {
            throw new DomainException(DomainErrorCode.NOTIFICATION_ALREADY_READ);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.NOTIFICATION_ALREADY_DELETED);
        }
    }
}
