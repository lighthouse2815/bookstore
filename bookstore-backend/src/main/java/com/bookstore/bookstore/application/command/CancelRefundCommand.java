package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record CancelRefundCommand(UUID refundId, UUID processedBy, String reason) {
}
