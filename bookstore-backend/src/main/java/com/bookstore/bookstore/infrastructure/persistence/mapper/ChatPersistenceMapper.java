package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.ChatMessage;
import com.bookstore.bookstore.domain.model.Conversation;
import com.bookstore.bookstore.domain.model.ConversationParticipant;
import com.bookstore.bookstore.infrastructure.persistence.entity.ChatMessageJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ConversationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ConversationParticipantJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ChatPersistenceMapper {

    public Conversation toDomain(ConversationJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Conversation(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getAssignedStaff() == null ? null : entity.getAssignedStaff().getId(),
                entity.getStatus(),
                entity.getSubject(),
                entity.getPriority(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getLastMessageId(),
                entity.getLastMessagePreview(),
                entity.getLastMessageAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getClosedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            ConversationJpaEntity entity,
            Conversation conversation,
            UserJpaEntity customer,
            UserJpaEntity assignedStaff
    ) {
        entity.setId(conversation.getId());
        entity.setCustomer(customer);
        entity.setAssignedStaff(assignedStaff);
        entity.setStatus(conversation.getStatus());
        entity.setSubject(conversation.getSubject());
        entity.setPriority(conversation.getPriority());
        entity.setTargetType(conversation.getTargetType());
        entity.setTargetId(conversation.getTargetId());
        entity.setLastMessageId(conversation.getLastMessageId());
        entity.setLastMessagePreview(conversation.getLastMessagePreview());
        entity.setLastMessageAt(conversation.getLastMessageAt());
        entity.setCreatedAt(conversation.getCreatedAt());
        entity.setUpdatedAt(conversation.getUpdatedAt());
        entity.setClosedAt(conversation.getClosedAt());
        entity.setDeletedAt(conversation.getDeletedAt());
    }

    public ChatMessage toDomain(ChatMessageJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ChatMessage(
                entity.getId(),
                entity.getConversation().getId(),
                entity.getSender().getId(),
                entity.getSenderRole(),
                entity.getMessageType(),
                entity.getContent(),
                entity.getAttachmentUrl(),
                entity.getAttachmentName(),
                entity.getAttachmentSize(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            ChatMessageJpaEntity entity,
            ChatMessage message,
            ConversationJpaEntity conversation,
            UserJpaEntity sender
    ) {
        entity.setId(message.getId());
        entity.setConversation(conversation);
        entity.setSender(sender);
        entity.setSenderRole(message.getSenderRole());
        entity.setMessageType(message.getMessageType());
        entity.setContent(message.getContent());
        entity.setAttachmentUrl(message.getAttachmentUrl());
        entity.setAttachmentName(message.getAttachmentName());
        entity.setAttachmentSize(message.getAttachmentSize());
        entity.setCreatedAt(message.getCreatedAt());
        entity.setUpdatedAt(message.getUpdatedAt());
        entity.setDeletedAt(message.getDeletedAt());
    }

    public ConversationParticipant toDomain(ConversationParticipantJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ConversationParticipant(
                entity.getId(),
                entity.getConversation().getId(),
                entity.getUser().getId(),
                entity.getRole(),
                entity.getLastReadMessageId(),
                entity.getLastReadAt(),
                entity.getUnreadCount(),
                entity.getJoinedAt(),
                entity.getLeftAt()
        );
    }

    public void copyToEntity(
            ConversationParticipantJpaEntity entity,
            ConversationParticipant participant,
            ConversationJpaEntity conversation,
            UserJpaEntity user
    ) {
        entity.setId(participant.getId());
        entity.setConversation(conversation);
        entity.setUser(user);
        entity.setRole(participant.getRole());
        entity.setLastReadMessageId(participant.getLastReadMessageId());
        entity.setLastReadAt(participant.getLastReadAt());
        entity.setUnreadCount(participant.getUnreadCount());
        entity.setJoinedAt(participant.getJoinedAt());
        entity.setLeftAt(participant.getLeftAt());
    }
}
