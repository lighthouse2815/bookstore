package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.SupplierRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Supplier {

    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public Supplier(
            UUID id,
            String name,
            String phone,
            String email,
            String address,
            String note,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_SUPPLIER_ID, "id");
        setName(name);
        setPhone(phone);
        setEmail(email);
        setAddress(address);
        setNote(note);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeletedAt(deletedAt);
    }

    public void updateSupplier(
            String name,
            String phone,
            String email,
            String address,
            String note
    ) {
        SupplierRule.requireCanUpdate(
                deletedAt,
                this.name,
                this.phone,
                this.email,
                this.address,
                this.note,
                name,
                phone,
                email,
                address,
                note
        );
        setName(name);
        setPhone(phone);
        setEmail(email);
        setAddress(address);
        setNote(note);
        setUpdatedAt(Instant.now());
    }

    public void softDelete() {
        SupplierRule.requireCanSoftDelete(deletedAt);
        Instant now = Instant.now();
        setUpdatedAt(now);
        setDeletedAt(now);
    }

    private void setName(String name) {
        this.name = Guard.notBlank(name, DomainErrorCode.INVALID_SUPPLIER_NAME, "name");
    }

    private void setPhone(String phone) {
        this.phone = Guard.contactPhoneNumberOrNull(phone, DomainErrorCode.INVALID_SUPPLIER_PHONE, "phone");
    }

    private void setEmail(String email) {
        this.email = Guard.emailOrNull(email, DomainErrorCode.INVALID_SUPPLIER_EMAIL, "email");
    }

    private void setAddress(String address) {
        this.address = Guard.notBlankOrNull(address, DomainErrorCode.INVALID_SUPPLIER_ADDRESS, "address");
    }

    private void setNote(String note) {
        this.note = Guard.notBlankOrNull(note, DomainErrorCode.INVALID_SUPPLIER_NOTE, "note");
    }

    private void setCreatedAt(Instant createdAt) {
        Instant validCreatedAt = Guard.notInFuture(
                createdAt,
                DomainErrorCode.INVALID_SUPPLIER_CREATED_AT,
                "createdAt"
        );
        Guard.validateAuditTimestamps(
                validCreatedAt,
                this.updatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_SUPPLIER_CREATED_AT,
                DomainErrorCode.INVALID_SUPPLIER_UPDATED_AT,
                DomainErrorCode.INVALID_SUPPLIER_DELETED_AT,
                DomainErrorCode.INVALID_SUPPLIER_AUDIT_ORDER
        );
        this.createdAt = validCreatedAt;
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_SUPPLIER_UPDATED_AT,
                "updatedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                validUpdatedAt,
                this.deletedAt,
                DomainErrorCode.INVALID_SUPPLIER_CREATED_AT,
                DomainErrorCode.INVALID_SUPPLIER_UPDATED_AT,
                DomainErrorCode.INVALID_SUPPLIER_DELETED_AT,
                DomainErrorCode.INVALID_SUPPLIER_AUDIT_ORDER
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setDeletedAt(Instant deletedAt) {
        Instant validDeletedAt = Guard.notInFutureOrNull(
                deletedAt,
                DomainErrorCode.INVALID_SUPPLIER_DELETED_AT,
                "deletedAt"
        );
        Guard.validateAuditTimestamps(
                this.createdAt,
                this.updatedAt,
                validDeletedAt,
                DomainErrorCode.INVALID_SUPPLIER_CREATED_AT,
                DomainErrorCode.INVALID_SUPPLIER_UPDATED_AT,
                DomainErrorCode.INVALID_SUPPLIER_DELETED_AT,
                DomainErrorCode.INVALID_SUPPLIER_AUDIT_ORDER
        );
        this.deletedAt = validDeletedAt;
    }
}
