package com.bookstore.bookstore.domain.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PasswordResetTokenRuleTest {

    @Test
    void requireCanUse_rejectsUsedToken() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        DomainException exception = assertThrows(
                DomainException.class,
                () -> PasswordResetTokenRule.requireCanUse(now.minusSeconds(1), now.plusSeconds(1), now)
        );

        assertEquals(DomainErrorCode.PASSWORD_RESET_TOKEN_ALREADY_USED, exception.getErrorCode());
    }

    @Test
    void requireCanUse_rejectsExpiredToken() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        DomainException exception = assertThrows(
                DomainException.class,
                () -> PasswordResetTokenRule.requireCanUse(null, now, now)
        );

        assertEquals(DomainErrorCode.PASSWORD_RESET_TOKEN_EXPIRED, exception.getErrorCode());
    }
}
