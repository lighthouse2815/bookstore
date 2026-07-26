package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BookshelfItem {

    private UUID id;
    private UUID shelfId;
    private UUID bookId;
    private int sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public BookshelfItem(
            UUID id,
            UUID shelfId,
            UUID bookId,
            int sortOrder,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_BOOKSHELF_ITEM_ID, "id");
        this.shelfId = Guard.notNull(shelfId, DomainErrorCode.INVALID_BOOKSHELF_ITEM_SHELF_ID, "shelfId");
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_BOOKSHELF_ITEM_BOOK_ID, "bookId");
        setSortOrder(sortOrder);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void moveTo(int sortOrder) {
        if (isDeleted()) {
            throw new DomainException(DomainErrorCode.BOOKSHELF_ITEM_ALREADY_DELETED);
        }

        if (this.sortOrder == sortOrder) {
            return;
        }

        setSortOrder(sortOrder);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        if (isDeleted()) {
            throw new DomainException(DomainErrorCode.BOOKSHELF_ITEM_ALREADY_DELETED);
        }

        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void restore(int sortOrder) {
        if (!isDeleted()) {
            throw new DomainException(DomainErrorCode.BOOKSHELF_ITEM_ALREADY_ACTIVE);
        }

        Instant now = Instant.now();
        setSortOrder(sortOrder);
        setDeletedAt(null);
        setUpdatedAt(now);
    }

    private void setSortOrder(int sortOrder) {
        if (sortOrder < 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOKSHELF_ITEM_SORT_ORDER, "sortOrder");
        }
        this.sortOrder = sortOrder;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_CREATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_DELETED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_CREATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_DELETED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_CREATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_UPDATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_DELETED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_ITEM_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
