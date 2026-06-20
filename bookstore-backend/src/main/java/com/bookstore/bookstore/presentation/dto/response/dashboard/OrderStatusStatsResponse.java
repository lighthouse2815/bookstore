package com.bookstore.bookstore.presentation.dto.response.dashboard;

public record OrderStatusStatsResponse(
        String status,
        long count
) {
}
