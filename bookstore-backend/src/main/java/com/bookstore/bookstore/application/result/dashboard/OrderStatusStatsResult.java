package com.bookstore.bookstore.application.result.dashboard;

import com.bookstore.bookstore.domain.enums.OrderStatus;

public record OrderStatusStatsResult(
        OrderStatus status,
        long count
) {
}
