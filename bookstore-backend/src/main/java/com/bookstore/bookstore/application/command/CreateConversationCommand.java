package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.domain.enums.ConversationPriority;
import com.bookstore.bookstore.domain.enums.ConversationTargetType;
import java.util.UUID;

public record CreateConversationCommand(
        UUID customerId,
        String subject,
        ConversationPriority priority,
        ConversationTargetType targetType,
        UUID targetId
) {
}
