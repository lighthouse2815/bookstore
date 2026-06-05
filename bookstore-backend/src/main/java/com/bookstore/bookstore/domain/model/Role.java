package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.RoleName;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Role {

    private UUID id;
    private RoleName name;
    private String description;
    private Set<Permission> permissions = new LinkedHashSet<>();
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Role(
            UUID id,
            RoleName name,
            String description,
            Set<Permission> permissions,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_ROLE_ID, "id");
        setName(name);
        setDescription(description);
        setPermissions(permissions);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    private void setName(RoleName name) {
        this.name = Guard.notNull(name, DomainErrorCode.INVALID_ROLE_NAME, "name");
    }

    private void setDescription(String description) {
        this.description = Guard.notBlank(description, DomainErrorCode.INVALID_ROLE_DESCRIPTION, "description");
    }

    private void setPermissions(Set<Permission> permissions) {
        this.permissions = new LinkedHashSet<>(
                Guard.noNullElements(permissions, DomainErrorCode.INVALID_ROLE_PERMISSIONS, "permissions")
        );
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(createdAt, DomainErrorCode.INVALID_ROLE_CREATED_AT, "createdAt");
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_ROLE_CREATED_AT,
                DomainErrorCode.INVALID_ROLE_UPDATED_AT,
                DomainErrorCode.INVALID_ROLE_DELETED_AT,
                DomainErrorCode.INVALID_ROLE_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_ROLE_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_ROLE_CREATED_AT,
                DomainErrorCode.INVALID_ROLE_UPDATED_AT,
                DomainErrorCode.INVALID_ROLE_DELETED_AT,
                DomainErrorCode.INVALID_ROLE_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_ROLE_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_ROLE_CREATED_AT,
                DomainErrorCode.INVALID_ROLE_UPDATED_AT,
                DomainErrorCode.INVALID_ROLE_DELETED_AT,
                DomainErrorCode.INVALID_ROLE_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
