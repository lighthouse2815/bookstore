package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class UserOtpRule {

    private UserOtpRule() {
    }

    public static void requirePending(Instant verifiedAt, Instant invalidatedAt) {
        if (verifiedAt != null || invalidatedAt != null) {
            throw new DomainException(DomainErrorCode.USER_OTP_NOT_PENDING);
        }
    }

    public static void requireCanVerify(
            Instant verifiedAt,
            Instant invalidatedAt,
            Instant expiresAt,
            Instant verifiedAtCandidate
    ) {
        requirePending(verifiedAt, invalidatedAt);
        if (!expiresAt.isAfter(verifiedAtCandidate)) {
            throw new DomainException(DomainErrorCode.USER_OTP_EXPIRED);
        }
    }

    public static void requireAttemptAvailable(int attemptCount, int maxAttempts) {
        if (attemptCount >= maxAttempts) {
            throw new DomainException(DomainErrorCode.USER_OTP_ATTEMPT_LIMIT_REACHED);
        }
    }
}
