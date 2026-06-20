package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.ConversationParticipant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IConversationParticipantRepository {

    Optional<ConversationParticipant> findByConversationIdAndUserIdActive(UUID conversationId, UUID userId);

    List<ConversationParticipant> findAllByConversationIdActive(UUID conversationId);

    ConversationParticipant save(ConversationParticipant participant);

    List<ConversationParticipant> saveAll(List<ConversationParticipant> participants);
}
