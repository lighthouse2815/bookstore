package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.result.StockMovementResult;
import com.bookstore.bookstore.presentation.response.StockMovementResponse;
import org.springframework.stereotype.Component;

@Component
public class StockMovementWebMapper {

    public StockMovementResponse toResponse(StockMovementResult result) {
        return new StockMovementResponse(
                result.id(),
                result.bookId(),
                result.type(),
                result.quantity(),
                result.beforeQuantity(),
                result.afterQuantity(),
                result.referenceId(),
                result.referenceType(),
                result.note(),
                result.createdAt(),
                result.createdBy()
        );
    }
}
