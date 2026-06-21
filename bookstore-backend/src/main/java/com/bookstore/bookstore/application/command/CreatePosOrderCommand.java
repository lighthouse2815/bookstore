package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.util.List;
import java.util.UUID;

public record CreatePosOrderCommand(
        UUID staffUserId,
        String customerName,
        String customerPhone,
        PaymentMethod paymentMethod,
        String couponCode,
        List<CreatePosOrderItemCommand> items
) {
    public CreatePosOrderCommand {
        if (staffUserId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "staffUserId");
        }
        if (paymentMethod == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "paymentMethod");
        }
        if (items == null || items.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.CART_EMPTY);
        }

        customerName = StringUtils.trimToNull(customerName);
        customerPhone = StringUtils.trimToNull(customerPhone);
        couponCode = StringUtils.trimToNull(couponCode);
        items = List.copyOf(items);
    }
}
