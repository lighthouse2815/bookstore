package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserOtpRuleTest {

    @Test
    void requireCanVerify_rejectsExpiredOtp() {
        Instant expiresAt = Instant.parse("2026-01-01T00:00:00Z");
        DomainException exception = assertThrows(
                DomainException.class,
                () -> UserOtpRule.requireCanVerify(null, null, expiresAt, expiresAt)
        );

        assertEquals(DomainErrorCode.USER_OTP_EXPIRED, exception.getErrorCode());
    }

    @Test
    void requireAttemptAvailable_rejectsReachedLimit() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> UserOtpRule.requireAttemptAvailable(5, 5)
        );

        assertEquals(DomainErrorCode.USER_OTP_ATTEMPT_LIMIT_REACHED, exception.getErrorCode());
    }
}
