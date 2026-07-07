package com.bookstore.bookstore.application.result;

import com.bookstore.bookstore.domain.enums.OrderStatus;

public record OrderStatusStatsResult(
        OrderStatus status,
        long count
) {
}
