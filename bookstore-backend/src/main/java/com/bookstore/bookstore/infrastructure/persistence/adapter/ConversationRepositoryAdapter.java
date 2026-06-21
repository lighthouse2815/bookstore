package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IConversationRepository;
import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.domain.model.Conversation;
import com.bookstore.bookstore.infrastructure.persistence.entity.ConversationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ChatPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.ConversationJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryAdapter implements IConversationRepository {

    private static final Sort CONVERSATION_SORT = Sort.by(
            Sort.Order.desc("lastMessageAt").nullsLast(),
            Sort.Order.desc("createdAt")
    );

    private final ConversationJpaRepository conversationJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ChatPersistenceMapper chatPersistenceMapper;

    @Override
    public List<Conversation> findAllByCustomerIdActive(UUID customerId) {
        return conversationJpaRepository.findAllByCustomer_IdAndDeletedAtIsNull(customerId, CONVERSATION_SORT).stream()
                .map(chatPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Conversation> findByIdAndCustomerIdActive(UUID conversationId, UUID customerId) {
        return conversationJpaRepository.findByIdAndCustomer_IdAndDeletedAtIsNull(conversationId, customerId)
                .map(chatPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Conversation> findByIdActive(UUID conversationId) {
        return conversationJpaRepository.findByIdAndDeletedAtIsNull(conversationId)
                .map(chatPersistenceMapper::toDomain);
    }

    @Override
    public List<Conversation> findPageActive(ConversationStatus status, String keyword, int page, int size) {
        return conversationJpaRepository.searchActive(
                        status,
                        keyword,
                        PageRequest.of(page, size)
                ).stream()
                .map(chatPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countActive(ConversationStatus status, String keyword) {
        return conversationJpaRepository.searchActive(
                status,
                keyword,
                PageRequest.of(0, 1)
        ).getTotalElements();
    }

    @Override
    public Conversation save(Conversation conversation) {
        ConversationJpaEntity entity = conversationJpaRepository.findById(conversation.getId())
                .orElseGet(ConversationJpaEntity::new);
        UserJpaEntity customer = userJpaRepository.findById(conversation.getCustomerId())
                .orElseThrow(() -> new IllegalStateException("Customer not found: " + conversation.getCustomerId()));
        UserJpaEntity assignedStaff = conversation.getAssignedStaffId() == null
                ? null
                : userJpaRepository.findById(conversation.getAssignedStaffId())
                        .orElseThrow(() -> new IllegalStateException("Assigned staff not found: " + conversation.getAssignedStaffId()));

        chatPersistenceMapper.copyToEntity(entity, conversation, customer, assignedStaff);
        return chatPersistenceMapper.toDomain(conversationJpaRepository.save(entity));
    }
}
