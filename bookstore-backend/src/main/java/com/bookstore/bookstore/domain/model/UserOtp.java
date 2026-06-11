package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class UserOtp {

    private UUID id;
    private UUID userId;
    private String otpHash;
    private Instant expiresAt;
    private Instant verifiedAt;
    private Instant invalidatedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public UserOtp(
            UUID id,
            UUID userId,
            String otpHash,
            Instant expiresAt,
            Instant verifiedAt,
            Instant invalidatedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_USER_OTP_ID, "id");
        setUserId(userId);
        setOtpHash(otpHash);
        setVerifiedAt(verifiedAt);
        setInvalidatedAt(invalidatedAt);
        setUpdatedAt(updatedAt);
        setCreatedAt(createdAt);
        setExpiresAt(expiresAt);
    }

    public boolean isPending() {
        return verifiedAt == null && invalidatedAt == null;
    }

    public boolean isExpiredAt(Instant instant) {
        Instant validInstant = Guard.notNull(instant, DomainErrorCode.INVALID_USER_OTP_EXPIRES_AT, "instant");
        return !expiresAt.isAfter(validInstant);
    }

    public void markVerified(Instant verifiedAt) {
        setVerifiedAt(verifiedAt);
        setUpdatedAt(verifiedAt);
    }

    public void invalidate(Instant invalidatedAt) {
        setInvalidatedAt(invalidatedAt);
        setUpdatedAt(invalidatedAt);
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_USER_OTP_USER_ID, "userId");
    }

    private void setOtpHash(String otpHash) {
        this.otpHash = Guard.notBlank(otpHash, DomainErrorCode.INVALID_USER_OTP_HASH, "otpHash");
    }

    private void setExpiresAt(Instant expiresAt) {
        Instant validExpiresAt = Guard.notNull(
                expiresAt,
                DomainErrorCode.INVALID_USER_OTP_EXPIRES_AT,
                "expiresAt"
        );
        Guard.after(
                validExpiresAt,
                this.createdAt,
                DomainErrorCode.INVALID_USER_OTP_AUDIT_ORDER,
                "expiresAt",
                "createdAt"
        );
        this.expiresAt = validExpiresAt;
    }

    private void setVerifiedAt(Instant verifiedAt) {
        Instant validVerifiedAt = Guard.notInFutureOrNull(
                verifiedAt,
                DomainErrorCode.INVALID_USER_OTP_VERIFIED_AT,
                "verifiedAt"
        );
        Guard.notBefore(
                validVerifiedAt,
                this.createdAt,
                DomainErrorCode.INVALID_USER_OTP_AUDIT_ORDER,
                "verifiedAt",
                "createdAt"
        );
        this.verifiedAt = validVerifiedAt;
    }

    private void setInvalidatedAt(Instant invalidatedAt) {
        Instant validInvalidatedAt = Guard.notInFutureOrNull(
                invalidatedAt,
                DomainErrorCode.INVALID_USER_OTP_INVALIDATED_AT,
                "invalidatedAt"
        );
        Guard.notBefore(
                validInvalidatedAt,
                this.createdAt,
                DomainErrorCode.INVALID_USER_OTP_AUDIT_ORDER,
                "invalidatedAt",
                "createdAt"
        );
        this.invalidatedAt = validInvalidatedAt;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_USER_OTP_CREATED_AT,
                "createdAt"
        );
        Guard.notBefore(
                this.updatedAt,
                validCreatedAt,
                DomainErrorCode.INVALID_USER_OTP_AUDIT_ORDER,
                "updatedAt",
                "createdAt"
        );
        Guard.notBefore(
                this.verifiedAt,
                validCreatedAt,
                DomainErrorCode.INVALID_USER_OTP_AUDIT_ORDER,
                "verifiedAt",
                "createdAt"
        );
        Guard.notBefore(
                this.invalidatedAt,
                validCreatedAt,
                DomainErrorCode.INVALID_USER_OTP_AUDIT_ORDER,
                "invalidatedAt",
                "createdAt"
        );
        Guard.after(
                this.expiresAt,
                validCreatedAt,
                DomainErrorCode.INVALID_USER_OTP_AUDIT_ORDER,
                "expiresAt",
                "createdAt"
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_USER_OTP_UPDATED_AT,
                "updatedAt"
        );
        Guard.notBefore(
                validUpdatedAt,
                this.createdAt,
                DomainErrorCode.INVALID_USER_OTP_AUDIT_ORDER,
                "updatedAt",
                "createdAt"
        );
        this.updatedAt = validUpdatedAt;
    }
}
