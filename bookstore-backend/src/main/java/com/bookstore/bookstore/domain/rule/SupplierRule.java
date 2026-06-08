package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.Objects;

public final class SupplierRule {

    private SupplierRule() {
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            String currentName,
            String currentPhone,
            String currentEmail,
            String currentAddress,
            String currentNote,
            String newName,
            String newPhone,
            String newEmail,
            String newAddress,
            String newNote
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.SUPPLIER_ALREADY_DELETED);
        }

        if (Objects.equals(currentName, newName)
                && Objects.equals(currentPhone, newPhone)
                && Objects.equals(currentEmail, newEmail)
                && Objects.equals(currentAddress, newAddress)
                && Objects.equals(currentNote, newNote)) {
            throw new DomainException(DomainErrorCode.SUPPLIER_DATA_NOT_CHANGED);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.SUPPLIER_ALREADY_DELETED);
        }
    }
}
