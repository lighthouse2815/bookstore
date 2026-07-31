package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.UserDigitalAccessRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class UserDigitalAccess {

    private UUID id;
    private UUID userId;
    private UUID digitalAssetId;
    private DigitalAccessType accessType;
    private DigitalAccessStatus status;
    private UUID sourceOrderId;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public UserDigitalAccess(
            UUID id,
            UUID userId,
            UUID digitalAssetId,
            DigitalAccessType accessType,
            DigitalAccessStatus status,
            UUID sourceOrderId,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_ID, "id");
        setUserId(userId);
        setDigitalAssetId(digitalAssetId);
        setAccessType(accessType);
        setStatus(status);
        setSourceOrderId(sourceOrderId);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
        setExpiresAt(expiresAt);
    }

    public void softDelete() {
        UserDigitalAccessRule.requireCanSoftDelete(deletedAt);

        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void grant(UUID sourceOrderId, Instant expiresAt, Instant grantedAt) {
        setStatus(DigitalAccessStatus.ACTIVE);
        setSourceOrderId(sourceOrderId);
        setDeletedAt(null);
        setUpdatedAt(grantedAt);
        setExpiresAt(expiresAt);
    }

    public void revoke(Instant revokedAt) {
        UserDigitalAccessRule.requireCanRevoke(status);
        setStatus(DigitalAccessStatus.REVOKED);
        setUpdatedAt(revokedAt);
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_USER_ID, "userId");
    }

    private void setDigitalAssetId(UUID digitalAssetId) {
        this.digitalAssetId = Guard.notNull(
                digitalAssetId,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_DIGITAL_ASSET_ID,
                "digitalAssetId"
        );
    }

    private void setAccessType(DigitalAccessType accessType) {
        this.accessType = Guard.notNull(
                accessType,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_TYPE,
                "accessType"
        );
    }

    private void setStatus(DigitalAccessStatus status) {
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_STATUS, "status");
    }

    private void setSourceOrderId(UUID sourceOrderId) {
        this.sourceOrderId = sourceOrderId;
    }

    private void setExpiresAt(Instant expiresAt) {
        Guard.notBefore(
                expiresAt,
                this.createdAt,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_AUDIT_ORDER,
                "expiresAt",
                "createdAt"
        );
        this.expiresAt = expiresAt;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_CREATED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_UPDATED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_DELETED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_CREATED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_UPDATED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_DELETED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_CREATED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_UPDATED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_DELETED_AT,
                DomainErrorCode.INVALID_USER_DIGITAL_ACCESS_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
