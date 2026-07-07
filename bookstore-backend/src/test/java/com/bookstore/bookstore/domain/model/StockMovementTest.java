package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockMovementTest {

    @Test
    void constructor_acceptsValidSaleMovement() {
        StockMovement stockMovement = stockMovement(StockMovementType.SALE, 2, 10, 8);

        assertEquals(StockMovementType.SALE, stockMovement.getType());
        assertEquals(2, stockMovement.getQuantity());
        assertEquals(10, stockMovement.getBeforeQuantity());
        assertEquals(8, stockMovement.getAfterQuantity());
    }

    @Test
    void constructor_whenQuantitiesDoNotMatchType_rejects() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> stockMovement(StockMovementType.SALE, 2, 10, 9)
        );

        assertEquals(DomainErrorCode.STOCK_MOVEMENT_QUANTITY_MISMATCH, exception.getErrorCode());
    }

    private static StockMovement stockMovement(
            StockMovementType type,
            int quantity,
            int beforeQuantity,
            int afterQuantity
    ) {
        return new StockMovement(
                UUID.randomUUID(),
                UUID.randomUUID(),
                type,
                quantity,
                beforeQuantity,
                afterQuantity,
                UUID.randomUUID(),
                "ORDER",
                null,
                Instant.EPOCH,
                UUID.randomUUID()
        );
    }
}
