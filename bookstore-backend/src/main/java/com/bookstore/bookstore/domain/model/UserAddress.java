package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.UserAddressRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class UserAddress {

    private UUID id;
    private UUID userId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private boolean defaultAddress;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public UserAddress(
            UUID id,
            UUID userId,
            String receiverName,
            String receiverPhone,
            String receiverAddress,
            boolean defaultAddress,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_USER_ADDRESS_ID, "id");
        setUserId(userId);
        setReceiverName(receiverName);
        setReceiverPhone(receiverPhone);
        setReceiverAddress(receiverAddress);
        setDefaultAddress(defaultAddress);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updateAddressInfo(
            String receiverName,
            String receiverPhone,
            String receiverAddress
    ) {
        UserAddressRule.requireCanUpdate(deletedAt);
        Instant now = Instant.now();
        setReceiverName(receiverName);
        setReceiverPhone(receiverPhone);
        setReceiverAddress(receiverAddress);
        setUpdatedAt(now);
    }

    public void markAsDefault() {
        UserAddressRule.requireCanChangeDefault(deletedAt);
        if (defaultAddress) {
            return;
        }

        setDefaultAddress(true);
        setUpdatedAt(Instant.now());
    }

    public void unmarkAsDefault() {
        UserAddressRule.requireCanChangeDefault(deletedAt);
        if (!defaultAddress) {
            return;
        }

        setDefaultAddress(false);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        UserAddressRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setDefaultAddress(false);
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setUserId(UUID userId) {
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_USER_ADDRESS_USER_ID, "userId");
    }

    private void setReceiverName(String receiverName) {
        this.receiverName = Guard.notBlank(
                receiverName,
                DomainErrorCode.INVALID_USER_ADDRESS_RECEIVER_NAME,
                "receiverName"
        );
    }

    private void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = Guard.phoneNumber(
                receiverPhone,
                DomainErrorCode.INVALID_USER_ADDRESS_RECEIVER_PHONE,
                "receiverPhone"
        );
    }

    private void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = Guard.notBlank(
                receiverAddress,
                DomainErrorCode.INVALID_USER_ADDRESS_RECEIVER_ADDRESS,
                "receiverAddress"
        );
    }

    private void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_USER_ADDRESS_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_USER_ADDRESS_CREATED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_UPDATED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_DELETED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_USER_ADDRESS_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_USER_ADDRESS_CREATED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_UPDATED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_DELETED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_USER_ADDRESS_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_USER_ADDRESS_CREATED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_UPDATED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_DELETED_AT,
                DomainErrorCode.INVALID_USER_ADDRESS_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
