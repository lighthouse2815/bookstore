package com.bookstore.bookstore.domain.validation;

import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;

public final class Guard {

    private static final Clock CLOCK = Clock.systemUTC();

    private Guard() {
    }

    public static <T> T notNull(T value, DomainErrorCode errorCode, String fieldName) {
        if (value == null) {
            throw new DomainException(errorCode, fieldName);
        }
        return value;
    }

    public static String notBlank(String value, DomainErrorCode errorCode, String fieldName) {
        String normalized = notNull(value, errorCode, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new DomainException(errorCode, fieldName);
        }
        return normalized;
    }

    public static String notBlankOrNull(String value, DomainErrorCode errorCode, String fieldName) {
        return value == null ? null : notBlank(value, errorCode, fieldName);
    }

    public static String gmailEmail(String value, DomainErrorCode errorCode, String fieldName) {
        String normalized = notBlank(value, errorCode, fieldName);
        if (!normalized.toLowerCase(Locale.ROOT).endsWith("@gmail.com")) {
            throw new DomainException(errorCode, fieldName);
        }
        return normalized;
    }

    public static String phoneNumber(String value, DomainErrorCode errorCode, String fieldName) {
        String normalized = notBlank(value, errorCode, fieldName);
        if (!normalized.matches("0\\d{9}")) {
            throw new DomainException(errorCode, fieldName);
        }
        return normalized;
    }

    public static <T> Collection<T> noNullElements(Collection<T> value, DomainErrorCode errorCode, String fieldName) {
        Collection<T> normalized = notNull(value, errorCode, fieldName);
        for (T element : normalized) {
            if (element == null) {
                throw new DomainException(errorCode, fieldName);
            }
        }
        return normalized;
    }

    public static Instant notInFuture(Instant value, DomainErrorCode errorCode, String fieldName) {
        Instant normalized = notNull(value, errorCode, fieldName);
        if (normalized.isAfter(now())) {
            throw new DomainException(errorCode, fieldName);
        }
        return normalized;
    }

    public static Instant notInFutureOrNull(Instant value, DomainErrorCode errorCode, String fieldName) {
        return value == null ? null : notInFuture(value, errorCode, fieldName);
    }

    public static LocalDate notInFuture(LocalDate value, DomainErrorCode errorCode, String fieldName) {
        LocalDate normalized = notNull(value, errorCode, fieldName);
        if (normalized.isAfter(LocalDate.now(CLOCK))) {
            throw new DomainException(errorCode, fieldName);
        }
        return normalized;
    }

    public static LocalDate notInFutureOrNull(LocalDate value, DomainErrorCode errorCode, String fieldName) {
        return value == null ? null : notInFuture(value, errorCode, fieldName);
    }

    public static Instant inFuture(Instant value, DomainErrorCode errorCode, String fieldName) {
        Instant normalized = notNull(value, errorCode, fieldName);
        if (!normalized.isAfter(now())) {
            throw new DomainException(errorCode, fieldName);
        }
        return normalized;
    }

    public static void notBefore(
            Instant value,
            Instant reference,
            DomainErrorCode errorCode,
            String fieldName,
            String referenceFieldName
    ) {
        if (value != null && reference != null && value.isBefore(reference)) {
            throw new DomainException(errorCode, fieldName, referenceFieldName);
        }
    }

    public static void after(
            Instant value,
            Instant reference,
            DomainErrorCode errorCode,
            String fieldName,
            String referenceFieldName
    ) {
        if (value != null && reference != null && !value.isAfter(reference)) {
            throw new DomainException(errorCode, fieldName, referenceFieldName);
        }
    }

    public static void validateAuditTimestamps(
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            DomainErrorCode createdAtErrorCode,
            DomainErrorCode updatedAtErrorCode,
            DomainErrorCode deletedAtErrorCode,
            DomainErrorCode orderErrorCode
    ) {
        if (createdAt != null) {
            notInFuture(createdAt, createdAtErrorCode, "createdAt");
        }
        if (updatedAt != null) {
            notInFuture(updatedAt, updatedAtErrorCode, "updatedAt");
        }
        if (deletedAt != null) {
            notInFuture(deletedAt, deletedAtErrorCode, "deletedAt");
        }
        notBefore(updatedAt, createdAt, orderErrorCode, "updatedAt", "createdAt");
        notBefore(deletedAt, createdAt, orderErrorCode, "deletedAt", "createdAt");
        notBefore(deletedAt, updatedAt, orderErrorCode, "deletedAt", "updatedAt");
    }

    private static Instant now() {
        return Instant.now(CLOCK);
    }
}
