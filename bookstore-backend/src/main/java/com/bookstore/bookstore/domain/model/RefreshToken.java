package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason;
import com.bookstore.bookstore.domain.rule.RefreshTokenRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private UUID familyId;
    private UUID parentTokenId;
    private UUID replacedByTokenId;
    private String deviceId;
    private String deviceName;
    private String userAgent;
    private String ipAddress;
    private Instant issuedAt;
    private Instant lastUsedAt;
    private Instant revokedAt;
    private RefreshTokenRevokeReason revokeReason;
    private Instant expiresAt;
    private boolean revoked;
    private Instant createdAt;

    public RefreshToken(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            boolean revoked,
            Instant createdAt
    ) {
        this(
                id, userId, tokenHash, id, null, null, null, null, null, null,
                createdAt, createdAt, revoked ? createdAt : null,
                revoked ? RefreshTokenRevokeReason.LEGACY_REVOKED : null,
                expiresAt, revoked, createdAt
        );
    }

    public RefreshToken(
            UUID id,
            UUID userId,
            String tokenHash,
            UUID familyId,
            UUID parentTokenId,
            UUID replacedByTokenId,
            String deviceId,
            String deviceName,
            String userAgent,
            String ipAddress,
            Instant issuedAt,
            Instant lastUsedAt,
            Instant revokedAt,
            RefreshTokenRevokeReason revokeReason,
            Instant expiresAt,
            boolean revoked,
            Instant createdAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_REFRESH_TOKEN_ID, "id");
        setUserId(userId);
        setTokenHash(tokenHash);
        setCreatedAt(createdAt);
        this.familyId = Guard.notNull(familyId, DomainErrorCode.INVALID_REFRESH_TOKEN_ID, "familyId");
        this.parentTokenId = parentTokenId;
        this.replacedByTokenId = replacedByTokenId;
        this.deviceId = normalize(deviceId, 128);
        this.deviceName = normalize(deviceName, 160);
        this.userAgent = normalize(userAgent, 500);
        this.ipAddress = normalize(ipAddress, 64);
        this.issuedAt = Guard.notInFuture(issuedAt, DomainErrorCode.INVALID_REFRESH_TOKEN_CREATED_AT, "issuedAt");
        this.lastUsedAt = Guard.notInFuture(lastUsedAt, DomainErrorCode.INVALID_REFRESH_TOKEN_CREATED_AT, "lastUsedAt");
        this.revokedAt = Guard.notInFutureOrNull(revokedAt, DomainErrorCode.INVALID_REFRESH_TOKEN_CREATED_AT, "revokedAt");
        this.revokeReason = revokeReason;
        setRevoked(revoked || revokedAt != null);
        setExpiresAt(expiresAt);
    }

    public boolean isExpiredAt(Instant instant) {
        return !expiresAt.isAfter(Guard.notNull(instant, DomainErrorCode.INVALID_REFRESH_TOKEN_EXPIRES_AT, "instant"));
    }

    public void revoke(Instant at, RefreshTokenRevokeReason reason) {
        this.revoked = true;
        this.revokedAt = Guard.notInFuture(at, DomainErrorCode.INVALID_REFRESH_TOKEN_CREATED_AT, "revokedAt");
        this.revokeReason = reason;
    }

    /** Kept for domain callers created before revoke reasons were introduced. */
    public void revoke() {
        revoke(Instant.now(), RefreshTokenRevokeReason.LOGOUT);
    }

    public void rotateTo(UUID replacementTokenId, Instant at) {
        this.replacedByTokenId = Guard.notNull(replacementTokenId, DomainErrorCode.INVALID_REFRESH_TOKEN_ID, "replacementTokenId");
        revoke(at, RefreshTokenRevokeReason.ROTATED);
    }

    public void markUsed(Instant at) {
        this.lastUsedAt = Guard.notInFuture(at, DomainErrorCode.INVALID_REFRESH_TOKEN_CREATED_AT, "lastUsedAt");
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_REFRESH_TOKEN_USER_ID, "userId");
    }

    private void setTokenHash(String tokenHash) {
        this.tokenHash = Guard.notBlank(tokenHash, DomainErrorCode.INVALID_REFRESH_TOKEN_TOKEN, "tokenHash");
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

    private String normalize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }
}
