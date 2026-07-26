package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IChatMessageRepository;
import com.bookstore.bookstore.domain.model.ChatMessage;
import com.bookstore.bookstore.domain.enums.MessageSenderRole;
import com.bookstore.bookstore.infrastructure.persistence.entity.ChatMessageJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ConversationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ChatPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.ChatMessageJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.ConversationJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements IChatMessageRepository {

    private static final Sort MESSAGE_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );

    private final ChatMessageJpaRepository chatMessageJpaRepository;
    private final ConversationJpaRepository conversationJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ChatPersistenceMapper chatPersistenceMapper;

    @Override
    public List<ChatMessage> findPageByConversationIdActive(UUID conversationId, int page, int size) {
        return chatMessageJpaRepository.findAllByConversation_IdAndDeletedAtIsNull(
                        conversationId,
                        PageRequest.of(page, size, MESSAGE_SORT)
                ).stream()
                .map(chatPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByConversationIdActive(UUID conversationId) {
        return chatMessageJpaRepository.countByConversation_IdAndDeletedAtIsNull(conversationId);
    }

    @Override
    public long countBySenderIdAndRoleSince(UUID senderId, MessageSenderRole senderRole, Instant createdAt) {
        return chatMessageJpaRepository
                .countBySender_IdAndSenderRoleAndCreatedAtGreaterThanEqualAndDeletedAtIsNull(
                        senderId,
                        senderRole,
                        createdAt
                );
    }

    @Override
    public Optional<ChatMessage> findByIdActive(UUID messageId) {
        return chatMessageJpaRepository.findByIdAndDeletedAtIsNull(messageId)
                .map(chatPersistenceMapper::toDomain);
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        ChatMessageJpaEntity entity = chatMessageJpaRepository.findById(message.getId())
                .orElseGet(ChatMessageJpaEntity::new);
        ConversationJpaEntity conversation = conversationJpaRepository.findById(message.getConversationId())
                .orElseThrow(() -> new IllegalStateException("Conversation not found: " + message.getConversationId()));
        UserJpaEntity sender = userJpaRepository.findById(message.getSenderId())
                .orElseThrow(() -> new IllegalStateException("Sender not found: " + message.getSenderId()));

        chatPersistenceMapper.copyToEntity(entity, message, conversation, sender);
        return chatPersistenceMapper.toDomain(chatMessageJpaRepository.save(entity));
    }
}
