package com.bookstore.bookstore.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateReturnRequestCommand(
        UUID orderId,
        UUID userId,
        String reason,
        BigDecimal requestedRefundAmount
) {
}
