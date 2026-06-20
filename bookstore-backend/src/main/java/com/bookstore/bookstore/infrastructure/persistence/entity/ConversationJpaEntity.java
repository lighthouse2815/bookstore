package com.bookstore.bookstore.infrastructure.persistence.entity;

import com.bookstore.bookstore.domain.enums.ConversationPriority;
import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.domain.enums.ConversationTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "chat_conversations",
        indexes = {
                @Index(name = "idx_chat_conversation_customer", columnList = "customer_id"),
                @Index(name = "idx_chat_conversation_assigned_staff", columnList = "assigned_staff_id"),
                @Index(name = "idx_chat_conversation_status", columnList = "status"),
                @Index(name = "idx_chat_conversation_last_message_at", columnList = "last_message_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ConversationJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private UserJpaEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private UserJpaEntity assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status;

    @Column(nullable = false, length = 255)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ConversationTargetType targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "last_message_id")
    private UUID lastMessageId;

    @Column(name = "last_message_preview", length = 500)
    private String lastMessagePreview;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
