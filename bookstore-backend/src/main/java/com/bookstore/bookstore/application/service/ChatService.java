package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.ChatAssembler;
import com.bookstore.bookstore.application.command.AssignConversationCommand;
import com.bookstore.bookstore.application.command.CreateConversationCommand;
import com.bookstore.bookstore.application.command.SendChatMessageCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IChatService;
import com.bookstore.bookstore.application.port.out.IChatMessageRepository;
import com.bookstore.bookstore.application.port.out.IChatRealtimePublisher;
import com.bookstore.bookstore.application.port.out.IConversationParticipantRepository;
import com.bookstore.bookstore.application.port.out.IConversationRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.ChatMessageSliceResult;
import com.bookstore.bookstore.application.result.ConversationResult;
import com.bookstore.bookstore.application.result.ConversationSliceResult;
import com.bookstore.bookstore.domain.enums.ConversationPriority;
import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.domain.enums.ConversationTargetType;
import com.bookstore.bookstore.domain.enums.MessageSenderRole;
import com.bookstore.bookstore.domain.enums.MessageType;
import com.bookstore.bookstore.domain.model.ChatMessage;
import com.bookstore.bookstore.domain.model.Conversation;
import com.bookstore.bookstore.domain.model.ConversationParticipant;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STAFF = "STAFF";
    private static final String ROLE_USER = "USER";

    private final IConversationRepository conversationRepository;
    private final IChatMessageRepository chatMessageRepository;
    private final IConversationParticipantRepository conversationParticipantRepository;
    private final IUserRepository userRepository;
    private final IChatRealtimePublisher chatRealtimePublisher;
    private final ChatAssembler chatAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResult> getMyConversations(UUID userId) {
        requireCustomerUser(userId);
        return conversationRepository.findAllByCustomerIdActive(userId).stream()
                .map(conversation -> toConversationResult(conversation, userId))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationResult createConversation(CreateConversationCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        User customer = requireCustomerUser(command.customerId());
        Instant now = Instant.now();
        Conversation conversation = new Conversation(
                UUID.randomUUID(),
                customer.getId(),
                null,
                ConversationStatus.OPEN,
                defaultConversationSubject(command.subject(), command.targetType()),
                command.priority() == null ? ConversationPriority.NORMAL : command.priority(),
                command.targetType() == null ? ConversationTargetType.GENERAL : command.targetType(),
                command.targetId(),
                null,
                null,
                now,
                now,
                now,
                null,
                null
        );
        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant customerParticipant = new ConversationParticipant(
                UUID.randomUUID(),
                savedConversation.getId(),
                customer.getId(),
                MessageSenderRole.USER,
                null,
                null,
                0,
                now,
                null
        );
        conversationParticipantRepository.save(customerParticipant);

        publishConversationAfterCommit(savedConversation, List.of(customerParticipant));
        return toConversationResult(savedConversation, customer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResult getConversationDetail(UUID userId, UUID conversationId) {
        requireCustomerUser(userId);
        return toConversationResult(requireOwnedConversation(userId, conversationId), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatMessageSliceResult getMessages(UUID userId, UUID conversationId, int page, int size) {
        requireCustomerUser(userId);
        validatePageRequest(page, size);
        requireOwnedConversation(userId, conversationId);
        return toMessageSliceResult(conversationId, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageResult sendUserMessage(SendChatMessageCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        User customer = requireCustomerUser(command.senderId());
        Conversation conversation = requireOwnedConversation(customer.getId(), command.conversationId());
        Instant now = Instant.now();
        ChatMessage message = new ChatMessage(
                UUID.randomUUID(),
                conversation.getId(),
                customer.getId(),
                MessageSenderRole.USER,
                command.messageType() == null ? MessageType.TEXT : command.messageType(),
                StringUtils.trimToNull(command.content()),
                StringUtils.trimToNull(command.attachmentUrl()),
                StringUtils.trimToNull(command.attachmentName()),
                command.attachmentSize(),
                now,
                now,
                null
        );
        ChatMessage savedMessage = chatMessageRepository.save(message);
        conversation.applyMessage(savedMessage.getId(), buildMessagePreview(savedMessage.getContent()), savedMessage.getCreatedAt());
        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant customerParticipant = getOrCreateParticipant(
                savedConversation.getId(),
                customer.getId(),
                MessageSenderRole.USER,
                now
        );
        customerParticipant.markRead(savedMessage.getId());

        List<ConversationParticipant> participants = new ArrayList<>(conversationParticipantRepository.findAllByConversationIdActive(savedConversation.getId()));
        if (participants.stream().noneMatch(participant -> participant.getUserId().equals(customer.getId()))) {
            participants.add(customerParticipant);
        }

        List<ConversationParticipant> participantsToSave = new ArrayList<>();
        participantsToSave.add(customerParticipant);

        for (ConversationParticipant participant : participants) {
            if (!participant.getUserId().equals(customer.getId())
                    && participant.getRole() != MessageSenderRole.USER) {
                participant.incrementUnread();
                participantsToSave.add(participant);
            }
        }

        List<ConversationParticipant> savedParticipants = deduplicateParticipants(
                conversationParticipantRepository.saveAll(participantsToSave)
        );
        ChatMessageResult messageResult = chatAssembler.toMessageResult(savedMessage);
        publishMessageAfterCommit(messageResult, savedConversation, mergeParticipants(participants, savedParticipants));
        return messageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationResult markConversationRead(UUID userId, UUID conversationId) {
        requireCustomerUser(userId);
        Conversation conversation = requireOwnedConversation(userId, conversationId);
        Instant now = Instant.now();
        ConversationParticipant participant = getOrCreateParticipant(
                conversation.getId(),
                userId,
                MessageSenderRole.USER,
                now
        );
        participant.markRead(conversation.getLastMessageId());
        conversationParticipantRepository.save(participant);
        return toConversationResult(conversation, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationResult closeMyConversation(UUID userId, UUID conversationId) {
        requireCustomerUser(userId);
        Conversation conversation = requireOwnedConversation(userId, conversationId);
        conversation.close();
        Conversation savedConversation = conversationRepository.save(conversation);
        List<ConversationParticipant> participants = conversationParticipantRepository.findAllByConversationIdActive(conversation.getId());
        publishConversationAfterCommit(savedConversation, participants);
        return toConversationResult(savedConversation, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationSliceResult getAdminConversations(
            UUID adminUserId,
            ConversationStatus status,
            String keyword,
            int page,
            int size
    ) {
        requireAdminOrStaffUser(adminUserId);
        validatePageRequest(page, size);
        List<ConversationResult> items = conversationRepository.findPageActive(status, normalizeKeyword(keyword), page, size).stream()
                .map(conversation -> toConversationResult(conversation, adminUserId))
                .toList();
        return new ConversationSliceResult(
                items,
                conversationRepository.countActive(status, normalizeKeyword(keyword)),
                page,
                size
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResult getAdminConversationDetail(UUID adminUserId, UUID conversationId) {
        requireAdminOrStaffUser(adminUserId);
        return toConversationResult(requireConversation(conversationId), adminUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatMessageSliceResult getAdminMessages(UUID adminUserId, UUID conversationId, int page, int size) {
        requireAdminOrStaffUser(adminUserId);
        validatePageRequest(page, size);
        requireConversation(conversationId);
        return toMessageSliceResult(conversationId, page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageResult sendAdminMessage(SendChatMessageCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        User actor = requireAdminOrStaffUser(command.senderId());
        Conversation conversation = requireConversation(command.conversationId());
        Instant now = Instant.now();
        MessageSenderRole senderRole = resolveSenderRole(actor);
        ChatMessage message = new ChatMessage(
                UUID.randomUUID(),
                conversation.getId(),
                actor.getId(),
                senderRole,
                command.messageType() == null ? MessageType.TEXT : command.messageType(),
                StringUtils.trimToNull(command.content()),
                StringUtils.trimToNull(command.attachmentUrl()),
                StringUtils.trimToNull(command.attachmentName()),
                command.attachmentSize(),
                now,
                now,
                null
        );
        ChatMessage savedMessage = chatMessageRepository.save(message);

        if (conversation.getAssignedStaffId() == null) {
            conversation.assignStaff(actor.getId());
        }
        conversation.applyMessage(savedMessage.getId(), buildMessagePreview(savedMessage.getContent()), savedMessage.getCreatedAt());
        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant actorParticipant = getOrCreateParticipant(
                savedConversation.getId(),
                actor.getId(),
                senderRole,
                now
        );
        actorParticipant.markRead(savedMessage.getId());

        ConversationParticipant customerParticipant = getOrCreateParticipant(
                savedConversation.getId(),
                savedConversation.getCustomerId(),
                MessageSenderRole.USER,
                savedConversation.getCreatedAt()
        );
        customerParticipant.incrementUnread();

        List<ConversationParticipant> participants = conversationParticipantRepository.findAllByConversationIdActive(savedConversation.getId());
        List<ConversationParticipant> savedParticipants = deduplicateParticipants(
                conversationParticipantRepository.saveAll(List.of(actorParticipant, customerParticipant))
        );
        ChatMessageResult messageResult = chatAssembler.toMessageResult(savedMessage);
        publishMessageAfterCommit(messageResult, savedConversation, mergeParticipants(participants, savedParticipants));
        return messageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationResult assignConversation(AssignConversationCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        requireAdminOrStaffUser(command.actorUserId());
        Conversation conversation = requireConversation(command.conversationId());
        User assignee = requireAssignableStaff(command.assignedStaffId());
        conversation.assignStaff(assignee.getId());
        Conversation savedConversation = conversationRepository.save(conversation);

        ConversationParticipant assignedParticipant = getOrCreateParticipant(
                savedConversation.getId(),
                assignee.getId(),
                resolveSenderRole(assignee),
                Instant.now()
        );
        ConversationParticipant savedParticipant = conversationParticipantRepository.save(assignedParticipant);
        List<ConversationParticipant> participants = mergeParticipants(
                conversationParticipantRepository.findAllByConversationIdActive(savedConversation.getId()),
                List.of(savedParticipant)
        );
        publishConversationAfterCommit(savedConversation, participants);
        return toConversationResult(savedConversation, command.actorUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationResult markAdminConversationRead(UUID adminUserId, UUID conversationId) {
        User actor = requireAdminOrStaffUser(adminUserId);
        Conversation conversation = requireConversation(conversationId);
        ConversationParticipant participant = getOrCreateParticipant(
                conversation.getId(),
                actor.getId(),
                resolveSenderRole(actor),
                Instant.now()
        );
        participant.markRead(conversation.getLastMessageId());
        conversationParticipantRepository.save(participant);
        return toConversationResult(conversation, actor.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationResult closeConversation(UUID adminUserId, UUID conversationId) {
        requireAdminOrStaffUser(adminUserId);
        Conversation conversation = requireConversation(conversationId);
        conversation.close();
        Conversation savedConversation = conversationRepository.save(conversation);
        List<ConversationParticipant> participants = conversationParticipantRepository.findAllByConversationIdActive(savedConversation.getId());
        publishConversationAfterCommit(savedConversation, participants);
        return toConversationResult(savedConversation, adminUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationResult reopenConversation(UUID adminUserId, UUID conversationId) {
        requireAdminOrStaffUser(adminUserId);
        Conversation conversation = requireConversation(conversationId);
        conversation.reopen();
        Conversation savedConversation = conversationRepository.save(conversation);
        List<ConversationParticipant> participants = conversationParticipantRepository.findAllByConversationIdActive(savedConversation.getId());
        publishConversationAfterCommit(savedConversation, participants);
        return toConversationResult(savedConversation, adminUserId);
    }

    private Conversation requireOwnedConversation(UUID userId, UUID conversationId) {
        if (conversationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "conversationId");
        }

        Conversation conversation = requireConversation(conversationId);
        if (!conversation.getCustomerId().equals(userId)) {
            throw new ApplicationException(ApplicationErrorCode.CHAT_CONVERSATION_FORBIDDEN);
        }
        return conversation;
    }

    private Conversation requireConversation(UUID conversationId) {
        if (conversationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "conversationId");
        }

        return conversationRepository.findByIdActive(conversationId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CHAT_CONVERSATION_NOT_FOUND));
    }

    private User requireCustomerUser(UUID userId) {
        User user = requireExistingUser(userId);
        if (!user.hasRole(ROLE_USER)) {
            throw new ApplicationException(ApplicationErrorCode.CHAT_CUSTOMER_ROLE_REQUIRED);
        }
        return user;
    }

    private User requireAdminOrStaffUser(UUID userId) {
        User user = requireExistingUser(userId);
        if (!user.hasRole(ROLE_ADMIN) && !user.hasRole(ROLE_STAFF)) {
            throw new ApplicationException(ApplicationErrorCode.CHAT_ADMIN_ROLE_REQUIRED);
        }
        return user;
    }

    private User requireAssignableStaff(UUID userId) {
        User assignee = userRepository.findByIdActive(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CHAT_ASSIGNEE_NOT_FOUND));
        if (!assignee.hasRole(ROLE_ADMIN) && !assignee.hasRole(ROLE_STAFF)) {
            throw new ApplicationException(ApplicationErrorCode.CHAT_ASSIGNEE_ROLE_INVALID);
        }
        return assignee;
    }

    private User requireExistingUser(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        return userRepository.findByIdActive(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));
    }

    private ConversationResult toConversationResult(Conversation conversation, UUID viewerUserId) {
        long unreadCount = 0;
        if (viewerUserId != null) {
            unreadCount = conversationParticipantRepository.findByConversationIdAndUserIdActive(
                            conversation.getId(),
                            viewerUserId
                    )
                    .map(ConversationParticipant::getUnreadCount)
                    .orElse(0);
        }
        User customer = userRepository.findByIdActive(conversation.getCustomerId()).orElse(null);
        User assignedStaff = conversation.getAssignedStaffId() == null
                ? null
                : userRepository.findByIdActive(conversation.getAssignedStaffId()).orElse(null);
        return chatAssembler.toConversationResult(
                conversation,
                customer == null ? null : customer.getUsername(),
                customer == null ? null : customer.getEmail(),
                assignedStaff == null ? null : assignedStaff.getUsername(),
                assignedStaff == null ? null : assignedStaff.getEmail(),
                unreadCount
        );
    }

    private ChatMessageSliceResult toMessageSliceResult(UUID conversationId, int page, int size) {
        List<ChatMessageResult> items = chatMessageRepository.findPageByConversationIdActive(conversationId, page, size).stream()
                .map(chatAssembler::toMessageResult)
                .toList();
        List<ChatMessageResult> ascendingItems = new ArrayList<>(items);
        java.util.Collections.reverse(ascendingItems);
        return new ChatMessageSliceResult(
                ascendingItems,
                chatMessageRepository.countByConversationIdActive(conversationId),
                page,
                size
        );
    }

    private ConversationParticipant getOrCreateParticipant(
            UUID conversationId,
            UUID userId,
            MessageSenderRole role,
            Instant joinedAt
    ) {
        return conversationParticipantRepository.findByConversationIdAndUserIdActive(conversationId, userId)
                .orElseGet(() -> new ConversationParticipant(
                        UUID.randomUUID(),
                        conversationId,
                        userId,
                        role,
                        null,
                        null,
                        0,
                        joinedAt,
                        null
                ));
    }

    private List<ConversationParticipant> deduplicateParticipants(List<ConversationParticipant> participants) {
        Map<UUID, ConversationParticipant> byUserId = new LinkedHashMap<>();
        for (ConversationParticipant participant : participants) {
            byUserId.put(participant.getUserId(), participant);
        }
        return new ArrayList<>(byUserId.values());
    }

    private List<ConversationParticipant> mergeParticipants(
            List<ConversationParticipant> baseParticipants,
            List<ConversationParticipant> overridingParticipants
    ) {
        Map<UUID, ConversationParticipant> byUserId = new LinkedHashMap<>();
        for (ConversationParticipant participant : baseParticipants) {
            byUserId.put(participant.getUserId(), participant);
        }
        for (ConversationParticipant participant : overridingParticipants) {
            byUserId.put(participant.getUserId(), participant);
        }
        return new ArrayList<>(byUserId.values());
    }

    private String defaultConversationSubject(String subject, ConversationTargetType targetType) {
        String normalizedSubject = StringUtils.trimToNull(subject);
        if (normalizedSubject != null) {
            return normalizedSubject;
        }

        if (ConversationTargetType.ORDER.equals(targetType)) {
            return "Ho tro don hang";
        }

        if (ConversationTargetType.BOOK.equals(targetType)) {
            return "Ho tro sach";
        }

        return "Ho tro khach hang";
    }

    private String buildMessagePreview(String content) {
        String normalized = content == null ? "" : content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 180) {
            return normalized;
        }
        return normalized.substring(0, 180);
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.trimToNull(keyword);
    }

    private MessageSenderRole resolveSenderRole(User user) {
        if (user.hasRole(ROLE_ADMIN)) {
            return MessageSenderRole.ADMIN;
        }
        if (user.hasRole(ROLE_STAFF)) {
            return MessageSenderRole.STAFF;
        }
        return MessageSenderRole.USER;
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }
        if (size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "size");
        }
    }

    private void publishConversationAfterCommit(Conversation conversation, List<ConversationParticipant> participants) {
        publishAfterCommit(null, conversation, participants);
    }

    private void publishMessageAfterCommit(
            ChatMessageResult message,
            Conversation conversation,
            List<ConversationParticipant> participants
    ) {
        publishAfterCommit(message, conversation, participants);
    }

    private void publishAfterCommit(
            ChatMessageResult message,
            Conversation conversation,
            List<ConversationParticipant> participants
    ) {
        List<ConversationParticipant> uniqueParticipants = deduplicateParticipants(participants);
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishRealtime(message, conversation, uniqueParticipants);
                }
            });
            return;
        }

        publishRealtime(message, conversation, uniqueParticipants);
    }

    private void publishRealtime(
            ChatMessageResult message,
            Conversation conversation,
            List<ConversationParticipant> participants
    ) {
        ConversationResult adminConversation = toConversationResult(conversation, null);
        try {
            chatRealtimePublisher.publishConversationToAdminTopic(adminConversation);
        } catch (RuntimeException exception) {
            log.warn("Failed to publish admin conversation update {}", conversation.getId(), exception);
        }

        for (ConversationParticipant participant : participants) {
            try {
                if (message != null) {
                    chatRealtimePublisher.publishMessageToUser(participant.getUserId().toString(), message);
                }
                chatRealtimePublisher.publishConversationToUser(
                        participant.getUserId().toString(),
                        toConversationResult(conversation, participant.getUserId())
                );
            } catch (RuntimeException exception) {
                log.warn(
                        "Failed to publish chat update for conversation {} to user {}",
                        conversation.getId(),
                        participant.getUserId(),
                        exception
                );
            }
        }
    }
}
