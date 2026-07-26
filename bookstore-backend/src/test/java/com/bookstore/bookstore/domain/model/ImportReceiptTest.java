package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ImportReceiptTest {

    @Test
    void constructor_acceptsValidReceipt() {
        ImportReceipt importReceipt = importReceipt(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("30.00"), importReceipt.getTotalAmount());
        assertEquals(1, importReceipt.getItems().size());
    }

    @Test
    void constructor_whenTotalAmountMismatch_rejects() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> importReceipt(new BigDecimal("20.00"))
        );

        assertEquals(DomainErrorCode.IMPORT_RECEIPT_TOTAL_AMOUNT_MISMATCH, exception.getErrorCode());
    }

    private static ImportReceipt importReceipt(BigDecimal totalAmount) {
        Instant now = Instant.EPOCH;
        ImportReceiptItem item = new ImportReceiptItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Book Title",
                new BigDecimal("10.00"),
                3,
                new BigDecimal("30.00")
        );

        return new ImportReceipt(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(item),
                totalAmount,
                "Nhap hang",
                now,
                now,
                UUID.randomUUID()
        );
    }
}
