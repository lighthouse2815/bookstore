package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignShipmentRequest(
        @NotNull(message = "orderId không được null")
        UUID orderId,
        @NotNull(message = "shipperId không được null")
        UUID shipperId
) {
}

