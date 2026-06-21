package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.ConversationResult;

public interface IChatRealtimePublisher {

    void publishMessageToUser(String userId, ChatMessageResult message);

    void publishConversationToUser(String userId, ConversationResult conversation);

    void publishConversationToAdminTopic(ConversationResult conversation);
}
