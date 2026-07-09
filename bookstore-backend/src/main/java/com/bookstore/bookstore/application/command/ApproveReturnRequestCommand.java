package com.bookstore.bookstore.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record ApproveReturnRequestCommand(
        UUID requestId,
        UUID adminUserId,
        String adminNote,
        BigDecimal approvedRefundAmount,
        boolean restock
) {
}
