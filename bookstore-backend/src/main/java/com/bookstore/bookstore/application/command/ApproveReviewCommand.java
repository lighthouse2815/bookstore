package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record ApproveReviewCommand(
        UUID reviewId,
        UUID adminUserId
) {
    public ApproveReviewCommand {
        if (reviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "reviewId");
        }
        if (adminUserId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "adminUserId");
        }
    }
}
