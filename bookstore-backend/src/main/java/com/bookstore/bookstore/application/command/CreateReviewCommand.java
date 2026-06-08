package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record CreateReviewCommand(
        UUID userId,
        UUID bookId,
        UUID orderItemId,
        int rating,
        String comment
) {
    public CreateReviewCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
        if (orderItemId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "orderItemId");
        }
    }
}
