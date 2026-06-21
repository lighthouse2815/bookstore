package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.util.UUID;

public record AssignShipmentCommand(
        UUID orderId,
        UUID shipperId
) {
    public AssignShipmentCommand {
        if (orderId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "orderId");
        }
        if (shipperId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipperId");
        }
    }
}
