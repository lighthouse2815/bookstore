package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class PasswordResetTokenRule {

    private PasswordResetTokenRule() {
    }

    public static void requireCanUse(Instant usedAt, Instant expiresAt, Instant usedAtCandidate) {
        if (usedAt != null) {
            throw new DomainException(DomainErrorCode.PASSWORD_RESET_TOKEN_ALREADY_USED);
        }
        if (!expiresAt.isAfter(usedAtCandidate)) {
            throw new DomainException(DomainErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }
    }
}
