package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ChatMessageJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"conversation", "sender"})
    Page<ChatMessageJpaEntity> findAllByConversation_IdAndDeletedAtIsNull(UUID conversationId, Pageable pageable);

    long countByConversation_IdAndDeletedAtIsNull(UUID conversationId);

    @EntityGraph(attributePaths = {"conversation", "sender"})
    Optional<ChatMessageJpaEntity> findByIdAndDeletedAtIsNull(UUID messageId);
}
