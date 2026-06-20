package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateOrderCommand(
        UUID userId,
        List<UUID> cartItemIds,
        UUID addressId,
        ShippingMethod shippingMethod,
        PaymentMethod paymentMethod,
        String bookCouponCode,
        String shippingCouponCode,
        String note
) {
    public CreateOrderCommand {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (shippingMethod == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shippingMethod");
        }
        if (paymentMethod == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "paymentMethod");
        }
        if (shippingMethod == ShippingMethod.DELIVERY && addressId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "addressId");
        }
        if (cartItemIds == null) {
            cartItemIds = List.of();
        } else {
            if (cartItemIds.stream().anyMatch(Objects::isNull)) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "cartItemIds");
            }
            cartItemIds = cartItemIds.stream()
                    .distinct()
                    .toList();
        }
        bookCouponCode = StringUtils.trimToNull(bookCouponCode);
        shippingCouponCode = StringUtils.trimToNull(shippingCouponCode);
        note = StringUtils.trimToNull(note);
    }
}
