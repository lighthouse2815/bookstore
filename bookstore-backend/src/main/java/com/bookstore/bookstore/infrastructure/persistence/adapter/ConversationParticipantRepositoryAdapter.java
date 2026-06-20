package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IConversationParticipantRepository;
import com.bookstore.bookstore.domain.model.ConversationParticipant;
import com.bookstore.bookstore.infrastructure.persistence.entity.ConversationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ConversationParticipantJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ChatPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.ConversationJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.ConversationParticipantJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationParticipantRepositoryAdapter implements IConversationParticipantRepository {

    private final ConversationParticipantJpaRepository conversationParticipantJpaRepository;
    private final ConversationJpaRepository conversationJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ChatPersistenceMapper chatPersistenceMapper;

    @Override
    public Optional<ConversationParticipant> findByConversationIdAndUserIdActive(UUID conversationId, UUID userId) {
        return conversationParticipantJpaRepository.findByConversation_IdAndUser_IdAndLeftAtIsNull(conversationId, userId)
                .map(chatPersistenceMapper::toDomain);
    }

    @Override
    public List<ConversationParticipant> findAllByConversationIdActive(UUID conversationId) {
        return conversationParticipantJpaRepository.findAllByConversation_IdAndLeftAtIsNull(conversationId).stream()
                .map(chatPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public ConversationParticipant save(ConversationParticipant participant) {
        ConversationParticipantJpaEntity entity = conversationParticipantJpaRepository.findById(participant.getId())
                .orElseGet(ConversationParticipantJpaEntity::new);
        ConversationJpaEntity conversation = conversationJpaRepository.findById(participant.getConversationId())
                .orElseThrow(() -> new IllegalStateException("Conversation not found: " + participant.getConversationId()));
        UserJpaEntity user = userJpaRepository.findById(participant.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + participant.getUserId()));

        chatPersistenceMapper.copyToEntity(entity, participant, conversation, user);
        return chatPersistenceMapper.toDomain(conversationParticipantJpaRepository.save(entity));
    }

    @Override
    public List<ConversationParticipant> saveAll(List<ConversationParticipant> participants) {
        return participants.stream()
                .map(this::save)
                .toList();
    }
}
