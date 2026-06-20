package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.ConversationPriority;
import com.bookstore.bookstore.domain.enums.ConversationTargetType;
import java.util.UUID;

public record CreateConversationRequest(
        String subject,
        ConversationPriority priority,
        ConversationTargetType targetType,
        UUID targetId
) {
}
