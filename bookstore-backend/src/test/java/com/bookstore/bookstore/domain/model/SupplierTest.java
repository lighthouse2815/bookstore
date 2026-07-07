package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SupplierTest {

    @Test
    void constructor_rejectsInvalidEmail() {
        Instant now = Instant.EPOCH;

        DomainException exception = assertThrows(
                DomainException.class,
                () -> new Supplier(
                        UUID.randomUUID(),
                        "Supplier A",
                        null,
                        "invalid-email",
                        null,
                        null,
                        now,
                        now,
                        null
                )
        );

        assertEquals(DomainErrorCode.INVALID_SUPPLIER_EMAIL, exception.getErrorCode());
    }

    @Test
    void updateSupplier_rejectsDeletedSupplier() {
        Instant now = Instant.EPOCH;
        Supplier supplier = new Supplier(
                UUID.randomUUID(),
                "Supplier A",
                null,
                null,
                null,
                null,
                now,
                now,
                now
        );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> supplier.updateSupplier("Supplier B", null, null, null, null)
        );

        assertEquals(DomainErrorCode.SUPPLIER_ALREADY_DELETED, exception.getErrorCode());
    }
}
