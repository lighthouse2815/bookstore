package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.domain.enums.RefundMethod;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateRefundCommand(
        UUID orderId, UUID returnRequestId, BigDecimal amount, String currency, String reason,
        RefundMethod method, String idempotencyKey, UUID requestedBy
) {
}
