package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "status khong duoc null")
        OrderStatus status
) {
}
