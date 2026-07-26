package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record SucceedRefundCommand(UUID refundId, UUID processedBy, String externalReference, String evidenceUrl, String evidenceMetadata) {
}
