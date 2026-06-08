package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class RefreshTokenRule {

    private RefreshTokenRule() {
    }

    public static void requireExpiresAfterCreatedAt(Instant expiresAt, Instant createdAt) {
        if (expiresAt != null && createdAt != null && !expiresAt.isAfter(createdAt)) {
            throw new DomainException(
                    DomainErrorCode.INVALID_REFRESH_TOKEN_AUDIT_ORDER,
                    "expiresAt",
                    "createdAt"
            );
        }
    }
}
