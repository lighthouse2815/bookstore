package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignShipmentRequest(
        @NotNull(message = "orderId khong duoc null")
        UUID orderId,
        @NotNull(message = "shipperId khong duoc null")
        UUID shipperId
) {
}
