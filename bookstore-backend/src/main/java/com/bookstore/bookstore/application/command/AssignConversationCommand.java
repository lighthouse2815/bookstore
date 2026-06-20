package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record AssignConversationCommand(
        UUID conversationId,
        UUID actorUserId,
        UUID assignedStaffId
) {
}
