package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record DeleteReviewCommand(
        UUID reviewId,
        UUID userId
) {
    public DeleteReviewCommand {
        if (reviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "reviewId");
        }
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
    }
}
