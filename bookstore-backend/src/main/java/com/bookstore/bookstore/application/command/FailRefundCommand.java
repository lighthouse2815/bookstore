package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record FailRefundCommand(UUID refundId, UUID processedBy, String failureReason) {
}
