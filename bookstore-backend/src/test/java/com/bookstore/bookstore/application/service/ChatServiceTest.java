package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.ChatAssembler;
import com.bookstore.bookstore.application.command.CreateConversationCommand;
import com.bookstore.bookstore.application.command.SendChatMessageCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IChatMessageRepository;
import com.bookstore.bookstore.application.port.out.IChatRealtimePublisher;
import com.bookstore.bookstore.application.port.out.IConversationParticipantRepository;
import com.bookstore.bookstore.application.port.out.IConversationRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.ConversationResult;
import com.bookstore.bookstore.domain.enums.ConversationPriority;
import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.domain.enums.ConversationTargetType;
import com.bookstore.bookstore.domain.enums.MessageSenderRole;
import com.bookstore.bookstore.domain.enums.MessageType;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Conversation;
import com.bookstore.bookstore.domain.model.ConversationParticipant;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private IConversationRepository conversationRepository;

    @Mock
    private IChatMessageRepository chatMessageRepository;

    @Mock
    private IConversationParticipantRepository conversationParticipantRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IChatRealtimePublisher chatRealtimePublisher;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(
                conversationRepository,
                chatMessageRepository,
                conversationParticipantRepository,
                userRepository,
                chatRealtimePublisher,
                new ChatAssembler()
        );
    }

    @Test
    void createConversation_whenCurrentUserIsNotCustomer_rejects() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdActive(userId)).thenReturn(Optional.of(adminUser(userId)));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> chatService.createConversation(new CreateConversationCommand(
                        userId,
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.CHAT_CUSTOMER_ROLE_REQUIRED, exception.getErrorCode());
    }

    @Test
    void getConversationDetail_whenConversationBelongsToOtherUser_rejectsForbidden() {
        UUID customerId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = conversation(conversationId, ownerId, ConversationStatus.OPEN);

        when(userRepository.findByIdActive(customerId)).thenReturn(Optional.of(customerUser(customerId)));
        when(conversationRepository.findByIdActive(conversationId)).thenReturn(Optional.of(conversation));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> chatService.getConversationDetail(customerId, conversationId)
        );

        assertEquals(ApplicationErrorCode.CHAT_CONVERSATION_FORBIDDEN, exception.getErrorCode());
    }

    @Test
    void sendUserMessage_updatesLastMessageAndIncrementsStaffUnread() {
        UUID customerId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = conversation(conversationId, customerId, ConversationStatus.OPEN);
        ConversationParticipant customerParticipant = participant(
                conversationId,
                customerId,
                MessageSenderRole.USER,
                0
        );
        ConversationParticipant staffParticipant = participant(
                conversationId,
                staffId,
                MessageSenderRole.STAFF,
                0
        );

        when(userRepository.findByIdActive(customerId)).thenReturn(Optional.of(customerUser(customerId)));
        when(conversationRepository.findByIdActive(conversationId)).thenReturn(Optional.of(conversation));
        when(chatMessageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(conversationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(conversationParticipantRepository.findByConversationIdAndUserIdActive(conversationId, customerId))
                .thenReturn(Optional.of(customerParticipant));
        when(conversationParticipantRepository.findByConversationIdAndUserIdActive(conversationId, staffId))
                .thenReturn(Optional.of(staffParticipant));
        when(conversationParticipantRepository.findAllByConversationIdActive(conversationId))
                .thenReturn(List.of(customerParticipant, staffParticipant));
        when(conversationParticipantRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessageResult result = chatService.sendUserMessage(new SendChatMessageCommand(
                conversationId,
                customerId,
                MessageType.TEXT,
                " Xin chao shop ",
                null,
                null,
                null
        ));

        ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(conversationCaptor.capture());
        assertEquals(result.messageId(), conversationCaptor.getValue().getLastMessageId());
        assertEquals("Xin chao shop", conversationCaptor.getValue().getLastMessagePreview());

        ArgumentCaptor<List<ConversationParticipant>> participantCaptor = ArgumentCaptor.forClass(List.class);
        verify(conversationParticipantRepository).saveAll(participantCaptor.capture());
        ConversationParticipant savedCustomerParticipant = participantCaptor.getValue().stream()
                .filter(participant -> participant.getUserId().equals(customerId))
                .findFirst()
                .orElseThrow();
        ConversationParticipant savedStaffParticipant = participantCaptor.getValue().stream()
                .filter(participant -> participant.getUserId().equals(staffId))
                .findFirst()
                .orElseThrow();

        assertEquals(0, savedCustomerParticipant.getUnreadCount());
        assertNotNull(savedCustomerParticipant.getLastReadAt());
        assertEquals(1, savedStaffParticipant.getUnreadCount());
        assertEquals("Xin chao shop", result.content());
    }

    @Test
    void markAdminConversationRead_resetsUnreadCountForCurrentAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID lastMessageId = UUID.randomUUID();
        Conversation conversation = new Conversation(
                conversationId,
                customerId,
                adminId,
                ConversationStatus.OPEN,
                "Ho tro khach hang",
                ConversationPriority.NORMAL,
                ConversationTargetType.GENERAL,
                null,
                lastMessageId,
                "Xin chao",
                Instant.EPOCH,
                Instant.EPOCH,
                Instant.EPOCH,
                null,
                null
        );
        ConversationParticipant adminParticipant = participant(
                conversationId,
                adminId,
                MessageSenderRole.ADMIN,
                3
        );

        when(userRepository.findByIdActive(adminId)).thenReturn(Optional.of(adminUser(adminId)));
        when(conversationRepository.findByIdActive(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationParticipantRepository.findByConversationIdAndUserIdActive(conversationId, adminId))
                .thenReturn(Optional.of(adminParticipant));
        when(conversationParticipantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ConversationResult result = chatService.markAdminConversationRead(adminId, conversationId);

        assertEquals(0, result.myUnreadCount());
        assertEquals(lastMessageId, adminParticipant.getLastReadMessageId());
        assertNotNull(adminParticipant.getLastReadAt());
        verify(conversationParticipantRepository).save(adminParticipant);
    }

    private static Conversation conversation(UUID conversationId, UUID customerId, ConversationStatus status) {
        Instant now = Instant.EPOCH;
        return new Conversation(
                conversationId,
                customerId,
                null,
                status,
                "Ho tro khach hang",
                ConversationPriority.NORMAL,
                ConversationTargetType.GENERAL,
                null,
                null,
                null,
                now,
                now,
                now,
                null,
                null
        );
    }

    private static ConversationParticipant participant(
            UUID conversationId,
            UUID userId,
            MessageSenderRole role,
            int unreadCount
    ) {
        return new ConversationParticipant(
                UUID.randomUUID(),
                conversationId,
                userId,
                role,
                null,
                null,
                unreadCount,
                Instant.EPOCH,
                null
        );
    }

    private static User customerUser(UUID userId) {
        return user(userId, Set.of(role("USER", "Khach hang")));
    }

    private static User adminUser(UUID userId) {
        return user(userId, Set.of(role("ADMIN", "Quan tri")));
    }

    private static User user(UUID userId, Set<Role> roles) {
        return new User(
                userId,
                "username",
                "password-hash",
                "0123456789",
                "test@gmail.com",
                UserStatus.ACTIVE,
                false,
                roles,
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
    }

    private static Role role(String name, String description) {
        return new Role(
                UUID.randomUUID(),
                name,
                description,
                Set.of(),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
    }
}
