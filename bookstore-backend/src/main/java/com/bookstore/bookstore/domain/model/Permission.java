package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.PermissionRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Permission {

    private UUID id;
    private PermissionCode code;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Permission(
            UUID id,
            PermissionCode code,
            String description,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_PERMISSION_ID, "id");
        setCode(code);
        setDescription(description);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updatePermission(PermissionCode code){
        PermissionRule.requireCanUpdate(deletedAt,this.code,code);
        setCode(code);
        setUpdatedAt(Instant.now());
    }

    private void setCode(PermissionCode code) {
        this.code = Guard.notNull(code, DomainErrorCode.INVALID_PERMISSION_CODE, "code");
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_PERMISSION_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_PERMISSION_CREATED_AT,
                DomainErrorCode.INVALID_PERMISSION_UPDATED_AT,
                DomainErrorCode.INVALID_PERMISSION_DELETED_AT,
                DomainErrorCode.INVALID_PERMISSION_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_PERMISSION_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_PERMISSION_CREATED_AT,
                DomainErrorCode.INVALID_PERMISSION_UPDATED_AT,
                DomainErrorCode.INVALID_PERMISSION_DELETED_AT,
                DomainErrorCode.INVALID_PERMISSION_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_PERMISSION_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_PERMISSION_CREATED_AT,
                DomainErrorCode.INVALID_PERMISSION_UPDATED_AT,
                DomainErrorCode.INVALID_PERMISSION_DELETED_AT,
                DomainErrorCode.INVALID_PERMISSION_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
