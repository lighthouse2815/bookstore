package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;

public final class StockMovementRule {

    private StockMovementRule() {
    }

    public static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_STOCK_MOVEMENT_QUANTITY, "quantity");
        }
    }

    public static void requireNonNegativeBeforeQuantity(int beforeQuantity) {
        if (beforeQuantity < 0) {
            throw new DomainException(
                    DomainErrorCode.INVALID_STOCK_MOVEMENT_BEFORE_QUANTITY,
                    "beforeQuantity"
            );
        }
    }

    public static void requireNonNegativeAfterQuantity(int afterQuantity) {
        if (afterQuantity < 0) {
            throw new DomainException(
                    DomainErrorCode.INVALID_STOCK_MOVEMENT_AFTER_QUANTITY,
                    "afterQuantity"
            );
        }
    }

    public static void requireConsistentQuantities(
            StockMovementType type,
            int quantity,
            int beforeQuantity,
            int afterQuantity
    ) {
        int delta = afterQuantity - beforeQuantity;

        boolean valid = switch (type) {
            case IMPORT, CANCEL_ORDER -> delta == quantity;
            case SALE -> delta == -quantity;
            case ADJUSTMENT -> Math.abs(delta) == quantity;
        };

        if (!valid) {
            throw new DomainException(DomainErrorCode.STOCK_MOVEMENT_QUANTITY_MISMATCH);
        }
    }
}
