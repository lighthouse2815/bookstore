package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ConversationParticipantJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationParticipantJpaRepository extends JpaRepository<ConversationParticipantJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"conversation", "user"})
    Optional<ConversationParticipantJpaEntity> findByConversation_IdAndUser_IdAndLeftAtIsNull(
            UUID conversationId,
            UUID userId
    );

    @EntityGraph(attributePaths = {"conversation", "user"})
    List<ConversationParticipantJpaEntity> findAllByConversation_IdAndLeftAtIsNull(UUID conversationId);
}
