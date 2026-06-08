package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.StockMovementResult;
import com.bookstore.bookstore.domain.model.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementAssembler {

    public StockMovementResult toResult(StockMovement stockMovement) {
        return new StockMovementResult(
                stockMovement.getId(),
                stockMovement.getBookId(),
                stockMovement.getType(),
                stockMovement.getQuantity(),
                stockMovement.getBeforeQuantity(),
                stockMovement.getAfterQuantity(),
                stockMovement.getReferenceId(),
                stockMovement.getReferenceType(),
                stockMovement.getNote(),
                stockMovement.getCreatedAt(),
                stockMovement.getCreatedBy()
        );
    }
}
