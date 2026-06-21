package com.bookstore.bookstore.infrastructure.chat;

import com.bookstore.bookstore.application.port.out.IChatRealtimePublisher;
import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.ConversationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatRealtimePublisherAdapter implements IChatRealtimePublisher {

    private static final Logger log = LoggerFactory.getLogger(ChatRealtimePublisherAdapter.class);

    private final SimpMessagingTemplate messagingTemplate;

    public ChatRealtimePublisherAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishMessageToUser(String userId, ChatMessageResult message) {
        try {
            messagingTemplate.convertAndSendToUser(userId, "/queue/chat/messages", message);
        } catch (RuntimeException exception) {
            log.warn("Failed to publish chat message {} to user {}", message.messageId(), userId, exception);
        }
    }

    @Override
    public void publishConversationToUser(String userId, ConversationResult conversation) {
        try {
            messagingTemplate.convertAndSendToUser(userId, "/queue/chat/conversations", conversation);
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to publish conversation update {} to user {}",
                    conversation.conversationId(),
                    userId,
                    exception
            );
        }
    }

    @Override
    public void publishConversationToAdminTopic(ConversationResult conversation) {
        try {
            messagingTemplate.convertAndSend("/topic/admin/chat/conversations", conversation);
        } catch (RuntimeException exception) {
            log.warn("Failed to publish admin conversation topic {}", conversation.conversationId(), exception);
        }
    }
}
