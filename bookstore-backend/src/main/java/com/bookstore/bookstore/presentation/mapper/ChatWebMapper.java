package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.AssignConversationCommand;
import com.bookstore.bookstore.application.command.CreateConversationCommand;
import com.bookstore.bookstore.application.command.SendChatMessageCommand;
import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.AiChatReplyResult;
import com.bookstore.bookstore.application.result.ConversationResult;
import com.bookstore.bookstore.presentation.request.AssignConversationRequest;
import com.bookstore.bookstore.presentation.request.CreateConversationRequest;
import com.bookstore.bookstore.presentation.request.SendChatMessageRequest;
import com.bookstore.bookstore.presentation.response.ChatMessageResponse;
import com.bookstore.bookstore.presentation.response.AiChatReplyResponse;
import com.bookstore.bookstore.presentation.response.ConversationResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ChatWebMapper {

    public CreateConversationCommand toCreateConversationCommand(CreateConversationRequest request, UUID customerId) {
        return new CreateConversationCommand(
                customerId,
                request.subject(),
                request.priority(),
                request.targetType(),
                request.targetId()
        );
    }

    public SendChatMessageCommand toSendMessageCommand(
            UUID conversationId,
            UUID senderId,
            SendChatMessageRequest request
    ) {
        return new SendChatMessageCommand(
                conversationId,
                senderId,
                request.messageType(),
                request.content(),
                request.attachmentUrl(),
                request.attachmentName(),
                request.attachmentSize()
        );
    }

    public AssignConversationCommand toAssignConversationCommand(
            UUID conversationId,
            UUID actorUserId,
            AssignConversationRequest request
    ) {
        return new AssignConversationCommand(conversationId, actorUserId, request.staffId());
    }

    public ConversationResponse toConversationResponse(ConversationResult result) {
        return new ConversationResponse(
                result.conversationId(),
                result.customerId(),
                result.customerName(),
                result.customerEmail(),
                result.assignedStaffId(),
                result.assignedStaffName(),
                result.assignedStaffEmail(),
                result.status(),
                result.subject(),
                result.priority(),
                result.targetType(),
                result.targetId(),
                result.lastMessageId(),
                result.lastMessagePreview(),
                result.lastMessageAt(),
                result.myUnreadCount(),
                result.createdAt(),
                result.updatedAt(),
                result.closedAt()
        );
    }

    public ChatMessageResponse toMessageResponse(ChatMessageResult result) {
        return new ChatMessageResponse(
                result.messageId(),
                result.conversationId(),
                result.senderId(),
                result.senderRole(),
                result.messageType(),
                result.content(),
                result.attachmentUrl(),
                result.attachmentName(),
                result.attachmentSize(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    public AiChatReplyResponse toAiChatReplyResponse(AiChatReplyResult result) {
        return new AiChatReplyResponse(
                result.status(),
                result.message() == null ? null : toMessageResponse(result.message()),
                result.remainingRequests()
        );
    }
}
