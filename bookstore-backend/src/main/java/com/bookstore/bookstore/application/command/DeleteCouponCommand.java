package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record DeleteCouponCommand(
        UUID couponId
) {
}
