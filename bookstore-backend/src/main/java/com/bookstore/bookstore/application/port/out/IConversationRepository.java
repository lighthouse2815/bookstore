package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.domain.model.Conversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IConversationRepository {

    List<Conversation> findAllByCustomerIdActive(UUID customerId);

    Optional<Conversation> findByIdAndCustomerIdActive(UUID conversationId, UUID customerId);

    Optional<Conversation> findByIdActive(UUID conversationId);

    List<Conversation> findPageActive(ConversationStatus status, String keyword, int page, int size);

    long countActive(ConversationStatus status, String keyword);

    Conversation save(Conversation conversation);
}
