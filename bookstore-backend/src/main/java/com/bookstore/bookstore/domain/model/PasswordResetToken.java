package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class PasswordResetToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant createdAt;

    public PasswordResetToken(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant usedAt,
            Instant createdAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_ID, "id");
        setUserId(userId);
        setTokenHash(tokenHash);
        setUsedAt(usedAt);
        setCreatedAt(createdAt);
        setExpiresAt(expiresAt);
    }

    public boolean isExpiredAt(Instant instant) {
        Instant validInstant = Guard.notNull(
                instant,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_EXPIRES_AT,
                "instant"
        );
        return !expiresAt.isAfter(validInstant);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markUsed(Instant usedAt) {
        setUsedAt(usedAt);
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_USER_ID, "userId");
    }

    private void setTokenHash(String tokenHash) {
        this.tokenHash = Guard.notBlank(
                tokenHash,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_HASH,
                "tokenHash"
        );
    }

    private void setExpiresAt(Instant expiresAt) {
        Instant validExpiresAt = Guard.notNull(
                expiresAt,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_EXPIRES_AT,
                "expiresAt"
        );
        Guard.after(
                validExpiresAt,
                this.createdAt,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_AUDIT_ORDER,
                "expiresAt",
                "createdAt"
        );
        this.expiresAt = validExpiresAt;
    }

    private void setUsedAt(Instant usedAt) {
        Instant validUsedAt = Guard.notInFutureOrNull(
                usedAt,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_USED_AT,
                "usedAt"
        );
        Guard.notBefore(
                validUsedAt,
                this.createdAt,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_AUDIT_ORDER,
                "usedAt",
                "createdAt"
        );
        this.usedAt = validUsedAt;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_CREATED_AT,
                "createdAt"
        );
        Guard.notBefore(
                this.usedAt,
                validCreatedAt,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_AUDIT_ORDER,
                "usedAt",
                "createdAt"
        );
        Guard.after(
                this.expiresAt,
                validCreatedAt,
                DomainErrorCode.INVALID_PASSWORD_RESET_TOKEN_AUDIT_ORDER,
                "expiresAt",
                "createdAt"
        );
        this.createdAt = validCreatedAt;
    }
}
