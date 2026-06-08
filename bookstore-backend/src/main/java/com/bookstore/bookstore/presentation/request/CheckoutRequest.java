package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CheckoutRequest(
        @NotNull(message = "addressId khong duoc null")
        UUID addressId,

        String couponCode
) {
}
