package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record RejectReturnRequestCommand(
        UUID requestId,
        UUID adminUserId,
        String adminNote
) {
}
