package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record CancelReturnRequestCommand(
        UUID requestId,
        UUID userId
) {
}
