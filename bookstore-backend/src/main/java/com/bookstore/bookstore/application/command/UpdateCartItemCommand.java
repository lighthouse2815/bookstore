package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record UpdateCartItemCommand(
        UUID userId,
        UUID itemReferenceId,
        int quantity
) {
    public UpdateCartItemCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (itemReferenceId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "itemReferenceId");
        }
        if (quantity <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "quantity");
        }
    }
}
