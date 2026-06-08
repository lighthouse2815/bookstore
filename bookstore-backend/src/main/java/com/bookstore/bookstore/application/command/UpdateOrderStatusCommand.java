package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import java.util.UUID;

public record UpdateOrderStatusCommand(
        UUID orderId,
        OrderStatus status
) {
    public UpdateOrderStatusCommand {
        if (orderId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "orderId");
        }
        if (status == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "status");
        }
    }
}
