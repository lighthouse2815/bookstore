package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record CreatePosOrderItemCommand(
        UUID bookId,
        int quantity
) {
    public CreatePosOrderItemCommand {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
        if (quantity <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "quantity");
        }
    }
}
