package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    @Test
    void revoke_marksTokenRevoked() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-value",
                now.minus(1, ChronoUnit.DAYS),
                false,
                now.minus(30, ChronoUnit.DAYS)
        );

        refreshToken.revoke();

        assertTrue(refreshToken.isRevoked());
    }

    @Test
    void isExpiredAt_returnsExpectedState() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-value",
                now.plus(1, ChronoUnit.DAYS),
                false,
                now
        );

        assertFalse(refreshToken.isExpiredAt(now));
        assertTrue(refreshToken.isExpiredAt(now.plus(2, ChronoUnit.DAYS)));
    }
}
