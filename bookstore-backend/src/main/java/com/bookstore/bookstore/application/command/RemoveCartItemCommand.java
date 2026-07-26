package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record RemoveCartItemCommand(
        UUID userId,
        UUID itemReferenceId
) {
    public RemoveCartItemCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (itemReferenceId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "itemReferenceId");
        }
    }
}
