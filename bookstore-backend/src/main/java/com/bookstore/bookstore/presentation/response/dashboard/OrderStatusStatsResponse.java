package com.bookstore.bookstore.presentation.response.dashboard;

public record OrderStatusStatsResponse(
        String status,
        long count
) {
}
