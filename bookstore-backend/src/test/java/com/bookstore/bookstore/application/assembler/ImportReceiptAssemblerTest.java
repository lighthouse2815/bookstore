package com.bookstore.bookstore.application.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bookstore.bookstore.application.result.ImportReceiptResult;
import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.domain.model.ImportReceiptItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ImportReceiptAssemblerTest {

    private final ImportReceiptAssembler importReceiptAssembler = new ImportReceiptAssembler();

    @Test
    void toResult_mapsReceiptAndItems() {
        ImportReceipt importReceipt = importReceipt();

        ImportReceiptResult result = importReceiptAssembler.toResult(importReceipt);

        assertEquals(importReceipt.getId(), result.id());
        assertEquals(importReceipt.getSupplierId(), result.supplierId());
        assertEquals(importReceipt.getTotalAmount(), result.totalAmount());
        assertEquals(importReceipt.getNote(), result.note());
        assertEquals(importReceipt.getCreatedAt(), result.createdAt());
        assertEquals(importReceipt.getUpdatedAt(), result.updatedAt());
        assertEquals(importReceipt.getCreatedBy(), result.createdBy());
        assertEquals(1, result.items().size());
        assertEquals(importReceipt.getItems().get(0).getId(), result.items().get(0).id());
        assertEquals(importReceipt.getItems().get(0).getBookId(), result.items().get(0).bookId());
        assertEquals(importReceipt.getItems().get(0).getBookTitle(), result.items().get(0).bookTitle());
        assertEquals(importReceipt.getItems().get(0).getUnitCost(), result.items().get(0).unitCost());
        assertEquals(importReceipt.getItems().get(0).getQuantity(), result.items().get(0).quantity());
        assertEquals(importReceipt.getItems().get(0).getLineTotal(), result.items().get(0).lineTotal());
    }

    private static ImportReceipt importReceipt() {
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
                new BigDecimal("30.00"),
                "Nhap hang",
                now,
                now,
                UUID.randomUUID()
        );
    }
}
