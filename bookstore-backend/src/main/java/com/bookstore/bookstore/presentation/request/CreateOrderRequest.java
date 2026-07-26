package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        List<UUID> cartItemIds,
        UUID addressId,

        @NotNull(message = "shippingMethod không được null")
        ShippingMethod shippingMethod,

        @NotNull(message = "paymentMethod không được null")
        PaymentMethod paymentMethod,

        String bookCouponCode,

        String shippingCouponCode,

        String note
) {
}

