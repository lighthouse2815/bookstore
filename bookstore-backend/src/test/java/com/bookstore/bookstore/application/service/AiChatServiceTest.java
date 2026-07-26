package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.port.in.IChatService;
import com.bookstore.bookstore.application.port.out.IAiChatClient;
import com.bookstore.bookstore.application.port.out.IAiChatSettings;
import com.bookstore.bookstore.application.port.out.IChatMessageRepository;
import com.bookstore.bookstore.application.result.AiChatReplyResult;
import com.bookstore.bookstore.application.result.AiChatReplyStatus;
import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.ChatMessageSliceResult;
import com.bookstore.bookstore.domain.enums.MessageSenderRole;
import com.bookstore.bookstore.domain.enums.MessageType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private IChatService chatService;

    @Mock
    private IChatMessageRepository chatMessageRepository;

    @Mock
    private IAiChatClient aiChatClient;

    @Mock
    private IAiChatSettings aiChatSettings;

    private AiChatService aiChatService;

    @BeforeEach
    void setUp() {
        aiChatService = new AiChatService(
                chatService,
                chatMessageRepository,
                aiChatClient,
                aiChatSettings
        );
        when(aiChatSettings.dailyUserLimit()).thenReturn(20);
        when(aiChatSettings.historyLimit()).thenReturn(12);
    }

    @Test
    void requestReply_whenDisabled_handsOffWithoutCallingProvider() {
        UUID userId = UUID.randomUUID();
        when(aiChatSettings.enabled()).thenReturn(false);

        AiChatReplyResult result = aiChatService.requestReply(userId, UUID.randomUUID());

        assertEquals(AiChatReplyStatus.DISABLED, result.status());
        assertNull(result.message());
        verify(aiChatClient, never()).generateReply(anyList());
    }

    @Test
    void requestReply_whenDailyLimitReached_doesNotCallProvider() {
        UUID userId = UUID.randomUUID();
        when(aiChatSettings.enabled()).thenReturn(true);
        when(aiChatClient.isConfigured()).thenReturn(true);
        when(chatMessageRepository.countBySenderIdAndRoleSince(
                any(),
                any(),
                any()
        )).thenReturn(20L);

        AiChatReplyResult result = aiChatService.requestReply(userId, UUID.randomUUID());

        assertEquals(AiChatReplyStatus.RATE_LIMITED, result.status());
        assertEquals(0, result.remainingRequests());
        verify(aiChatClient, never()).generateReply(anyList());
    }

    @Test
    void requestReply_whenUserMessageIsPending_persistsAiResponse() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        ChatMessageResult userMessage = message(
                UUID.randomUUID(),
                conversationId,
                userId,
                MessageSenderRole.USER,
                "Gợi ý cho tôi một cuốn tiểu thuyết"
        );
        ChatMessageResult aiMessage = message(
                UUID.randomUUID(),
                conversationId,
                userId,
                MessageSenderRole.SYSTEM,
                "Bạn có thể bắt đầu với Nhà giả kim."
        );

        when(aiChatSettings.enabled()).thenReturn(true);
        when(aiChatClient.isConfigured()).thenReturn(true);
        when(chatMessageRepository.countBySenderIdAndRoleSince(any(), any(), any())).thenReturn(0L);
        when(chatService.getMessages(userId, conversationId, 0, 12))
                .thenReturn(new ChatMessageSliceResult(List.of(userMessage), 1, 0, 12));
        when(aiChatClient.generateReply(anyList())).thenReturn(aiMessage.content());
        when(chatService.sendSystemMessage(userId, conversationId, aiMessage.content())).thenReturn(aiMessage);

        AiChatReplyResult result = aiChatService.requestReply(userId, conversationId);

        assertEquals(AiChatReplyStatus.ANSWERED, result.status());
        assertEquals(aiMessage, result.message());
        assertEquals(19, result.remainingRequests());
        verify(chatService).sendSystemMessage(userId, conversationId, aiMessage.content());
    }

    private static ChatMessageResult message(
            UUID messageId,
            UUID conversationId,
            UUID senderId,
            MessageSenderRole senderRole,
            String content
    ) {
        return new ChatMessageResult(
                messageId,
                conversationId,
                senderId,
                senderRole,
                MessageType.TEXT,
                content,
                null,
                null,
                null,
                Instant.EPOCH,
                Instant.EPOCH
        );
    }
}
