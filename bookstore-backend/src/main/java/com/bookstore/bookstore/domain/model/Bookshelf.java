package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Bookshelf {

    private UUID id;
    private UUID userId;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Bookshelf(
            UUID id,
            UUID userId,
            String name,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_BOOKSHELF_ID, "id");
        this.userId = Guard.notNull(userId, DomainErrorCode.INVALID_BOOKSHELF_USER_ID, "userId");
        setName(name);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void rename(String name) {
        if (isDeleted()) {
            throw new DomainException(DomainErrorCode.BOOKSHELF_ALREADY_DELETED);
        }

        setName(name);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        if (isDeleted()) {
            throw new DomainException(DomainErrorCode.BOOKSHELF_ALREADY_DELETED);
        }

        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    public void restore() {
        if (!isDeleted()) {
            throw new DomainException(DomainErrorCode.BOOKSHELF_ALREADY_ACTIVE);
        }

        Instant now = Instant.now();
        setDeletedAt(null);
        setUpdatedAt(now);
    }

    private void setName(String name) {
        this.name = Guard.notBlank(name, DomainErrorCode.INVALID_BOOKSHELF_NAME, "name");
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_BOOKSHELF_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_BOOKSHELF_CREATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_UPDATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_DELETED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_BOOKSHELF_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_BOOKSHELF_CREATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_UPDATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_DELETED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_BOOKSHELF_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_BOOKSHELF_CREATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_UPDATED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_DELETED_AT,
                DomainErrorCode.INVALID_BOOKSHELF_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
