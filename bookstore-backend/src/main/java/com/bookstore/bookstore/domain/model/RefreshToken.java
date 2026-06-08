package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.RefreshTokenRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String token;
    private Instant expiresAt;
    private boolean revoked;
    private Instant createdAt;

    public RefreshToken(
            UUID id,
            UUID userId,
            String token,
            Instant expiresAt,
            boolean revoked,
            Instant createdAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_REFRESH_TOKEN_ID, "id");
        setUserId(userId);
        setToken(token);
        setRevoked(revoked);
        setCreatedAt(createdAt);
        setExpiresAt(expiresAt);
    }

    public boolean isExpiredAt(Instant instant) {
        return !expiresAt.isAfter(Guard.notNull(instant, DomainErrorCode.INVALID_REFRESH_TOKEN_EXPIRES_AT, "instant"));
    }

    public void revoke() {
        this.revoked = true;
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_REFRESH_TOKEN_USER_ID, "userId");
    }

    private void setToken(String token) {
        this.token = Guard.notBlank(token, DomainErrorCode.INVALID_REFRESH_TOKEN_TOKEN, "token");
    }

    private void setExpiresAt(Instant expiresAt) {
        Instant validExpiresAt = Guard.notNull(
                expiresAt,
                DomainErrorCode.INVALID_REFRESH_TOKEN_EXPIRES_AT,
                "expiresAt"
        );
        RefreshTokenRule.requireExpiresAfterCreatedAt(validExpiresAt, this.createdAt);
        this.expiresAt = validExpiresAt;
    }

    private void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_REFRESH_TOKEN_CREATED_AT,
                "createdAt"
        );
        RefreshTokenRule.requireExpiresAfterCreatedAt(this.expiresAt, validCreatedAt);
        this.createdAt = validCreatedAt;
    }
}
