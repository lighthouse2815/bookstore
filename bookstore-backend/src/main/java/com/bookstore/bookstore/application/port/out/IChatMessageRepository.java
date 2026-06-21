package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.ChatMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IChatMessageRepository {

    List<ChatMessage> findPageByConversationIdActive(UUID conversationId, int page, int size);

    long countByConversationIdActive(UUID conversationId);

    Optional<ChatMessage> findByIdActive(UUID messageId);

    ChatMessage save(ChatMessage message);
}
