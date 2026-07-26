package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreatePosOrderRequest(
        String customerName,
        String customerPhone,

        @NotNull(message = "paymentMethod không được null")
        PaymentMethod paymentMethod,

        String couponCode,

        @NotEmpty(message = "items không được để trống")
        List<@Valid CreatePosOrderItemRequest> items
) {
}

