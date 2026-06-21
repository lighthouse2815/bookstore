package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(
        @NotNull(message = "status khong duoc null")
        ShipmentStatus status,
        String failureReason
) {
}
