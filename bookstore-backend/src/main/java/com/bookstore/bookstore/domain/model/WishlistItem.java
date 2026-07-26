package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class WishlistItem {

    private UUID id;
    private UUID userId;
    private UUID bookId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public WishlistItem(
            UUID id,
            UUID userId,
            UUID bookId,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_WISHLIST_ITEM_ID, "id");
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_WISHLIST_ITEM_USER_ID, "userId");
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_WISHLIST_ITEM_BOOK_ID, "bookId");
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        if (isDeleted()) {
            throw new DomainException(DomainErrorCode.WISHLIST_ITEM_ALREADY_DELETED);
        }

        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void restore() {
        if (!isDeleted()) {
            throw new DomainException(DomainErrorCode.WISHLIST_ITEM_ALREADY_ACTIVE);
        }

        Instant now = Instant.now();
        setDeletedAt(null);
        setUpdatedAt(now);
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_WISHLIST_ITEM_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_WISHLIST_ITEM_CREATED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_DELETED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_WISHLIST_ITEM_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_WISHLIST_ITEM_CREATED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_DELETED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_WISHLIST_ITEM_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_WISHLIST_ITEM_CREATED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_DELETED_AT,
                DomainErrorCode.INVALID_WISHLIST_ITEM_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
