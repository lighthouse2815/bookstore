package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.util.UUID;

public record CancelOrderCommand(UUID userId, UUID orderId, String reason) {

    public CancelOrderCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (orderId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "orderId");
        }
        reason = StringUtils.trimToNull(reason);
        if (reason == null || reason.length() > 500) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "reason");
        }
    }
}
