package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import java.util.UUID;

public record UpdateShipmentStatusCommand(
        UUID shipmentId,
        UUID shipperId,
        ShipmentStatus status,
        String failureReason
) {
    public UpdateShipmentStatusCommand {
        if (shipmentId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipmentId");
        }
        if (shipperId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipperId");
        }
        if (status == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "status");
        }
    }
}
