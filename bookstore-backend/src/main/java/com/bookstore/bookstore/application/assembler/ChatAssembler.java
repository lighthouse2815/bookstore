package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.ConversationResult;
import com.bookstore.bookstore.domain.model.ChatMessage;
import com.bookstore.bookstore.domain.model.Conversation;
import org.springframework.stereotype.Component;

@Component
public class ChatAssembler {

    public ConversationResult toConversationResult(
            Conversation conversation,
            String customerName,
            String customerEmail,
            String assignedStaffName,
            String assignedStaffEmail,
            long myUnreadCount
    ) {
        return new ConversationResult(
                conversation.getId(),
                conversation.getCustomerId(),
                customerName,
                customerEmail,
                conversation.getAssignedStaffId(),
                assignedStaffName,
                assignedStaffEmail,
                conversation.getStatus(),
                conversation.getSubject(),
                conversation.getPriority(),
                conversation.getTargetType(),
                conversation.getTargetId(),
                conversation.getLastMessageId(),
                conversation.getLastMessagePreview(),
                conversation.getLastMessageAt(),
                myUnreadCount,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getClosedAt()
        );
    }

    public ChatMessageResult toMessageResult(ChatMessage message) {
        return new ChatMessageResult(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getSenderRole(),
                message.getMessageType(),
                message.getContent(),
                message.getAttachmentUrl(),
                message.getAttachmentName(),
                message.getAttachmentSize(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
