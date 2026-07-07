package com.bookstore.bookstore.application.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bookstore.bookstore.application.result.StockMovementResult;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.StockMovement;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockMovementAssemblerTest {

    private final StockMovementAssembler stockMovementAssembler = new StockMovementAssembler();

    @Test
    void toResult_mapsAllFields() {
        StockMovement stockMovement = new StockMovement(
                UUID.randomUUID(),
                UUID.randomUUID(),
                StockMovementType.SALE,
                2,
                10,
                8,
                UUID.randomUUID(),
                "ORDER",
                "Checkout",
                Instant.EPOCH,
                UUID.randomUUID()
        );

        StockMovementResult result = stockMovementAssembler.toResult(stockMovement);

        assertEquals(stockMovement.getId(), result.id());
        assertEquals(stockMovement.getBookId(), result.bookId());
        assertEquals(stockMovement.getType(), result.type());
        assertEquals(stockMovement.getQuantity(), result.quantity());
        assertEquals(stockMovement.getBeforeQuantity(), result.beforeQuantity());
        assertEquals(stockMovement.getAfterQuantity(), result.afterQuantity());
        assertEquals(stockMovement.getReferenceId(), result.referenceId());
        assertEquals(stockMovement.getReferenceType(), result.referenceType());
        assertEquals(stockMovement.getNote(), result.note());
        assertEquals(stockMovement.getCreatedAt(), result.createdAt());
        assertEquals(stockMovement.getCreatedBy(), result.createdBy());
    }
}
