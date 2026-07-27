package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.port.in.IAiChatService;
import com.bookstore.bookstore.application.port.in.IChatService;
import com.bookstore.bookstore.application.port.out.IAiChatClient;
import com.bookstore.bookstore.application.port.out.IAiChatSettings;
import com.bookstore.bookstore.application.port.out.IChatMessageRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.AiChatReplyResult;
import com.bookstore.bookstore.application.result.AiChatReplyStatus;
import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.domain.enums.MessageSenderRole;
import com.bookstore.bookstore.domain.enums.MessageType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiChatService implements IAiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private static final int LOCK_STRIPES = 64;
    private static final String SYSTEM_PROMPT = """
            Bạn là Trợ lý AI của nhà sách SáchVui. Hãy trả lời bằng tiếng Việt tự nhiên, thân thiện và ngắn gọn.
            Bạn có thể tư vấn cách chọn sách, thể loại, tác giả và giải thích kiến thức chung về sách.
            Không được bịa tồn kho, giá hiện tại, trạng thái đơn hàng, chính sách hoặc dữ liệu tài khoản.
            Khi câu hỏi cần dữ liệu thật của cửa hàng, đơn hàng, thanh toán, đổi trả hoặc tài khoản,
            hãy nói rõ nhân viên SáchVui sẽ kiểm tra và phản hồi ngay trong cuộc trò chuyện này.
            Không yêu cầu khách cung cấp mật khẩu, OTP, số thẻ hoặc thông tin thanh toán nhạy cảm.
            """;

    private final IChatService chatService;
    private final IChatMessageRepository chatMessageRepository;
    private final IAiChatClient aiChatClient;
    private final IAiChatSettings aiChatSettings;
    private final Object[] requestLocks;

    public AiChatService(
            IChatService chatService,
            IChatMessageRepository chatMessageRepository,
            IAiChatClient aiChatClient,
            IAiChatSettings aiChatSettings
    ) {
        this.chatService = chatService;
        this.chatMessageRepository = chatMessageRepository;
        this.aiChatClient = aiChatClient;
        this.aiChatSettings = aiChatSettings;
        this.requestLocks = IntStream.range(0, LOCK_STRIPES)
                .mapToObj(ignored -> new Object())
                .toArray();
    }

    @Override
    public AiChatReplyResult requestReply(UUID userId, UUID conversationId) {
        Object requestLock = requestLocks[Math.floorMod(userId.hashCode(), requestLocks.length)];
        synchronized (requestLock) {
            return requestReplyLocked(userId, conversationId);
        }
    }

    private AiChatReplyResult requestReplyLocked(UUID userId, UUID conversationId) {
        int dailyLimit = Math.max(1, aiChatSettings.dailyUserLimit());
        int historyLimit = Math.min(PageQuery.MAX_SIZE, Math.max(2, aiChatSettings.historyLimit()));
        Instant startOfUtcDay = LocalDate.now(ZoneOffset.UTC)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        long usedToday = chatMessageRepository.countBySenderIdAndRoleSince(
                userId,
                MessageSenderRole.SYSTEM,
                startOfUtcDay
        );
        int remainingRequests = Math.max(0, dailyLimit - Math.toIntExact(Math.min(usedToday, dailyLimit)));

        if (!aiChatSettings.enabled() || !aiChatClient.isConfigured()) {
            return new AiChatReplyResult(AiChatReplyStatus.DISABLED, null, remainingRequests);
        }

        if (remainingRequests == 0) {
            return new AiChatReplyResult(AiChatReplyStatus.RATE_LIMITED, null, 0);
        }

        List<ChatMessageResult> history = chatService
                .getMessages(userId, conversationId, new PageQuery(PageQuery.DEFAULT_PAGE, historyLimit))
                .items();
        if (history.isEmpty()) {
            return new AiChatReplyResult(AiChatReplyStatus.NO_PENDING_MESSAGE, null, remainingRequests);
        }

        ChatMessageResult latestMessage = history.getLast();
        if (latestMessage.senderRole() == MessageSenderRole.SYSTEM) {
            return new AiChatReplyResult(
                    AiChatReplyStatus.ALREADY_ANSWERED,
                    latestMessage,
                    remainingRequests
            );
        }

        if (latestMessage.senderRole() != MessageSenderRole.USER
                || latestMessage.messageType() != MessageType.TEXT) {
            return new AiChatReplyResult(AiChatReplyStatus.NO_PENDING_MESSAGE, null, remainingRequests);
        }

        try {
            String reply = aiChatClient.generateReply(buildPrompt(history));
            ChatMessageResult savedMessage = chatService.sendSystemMessage(userId, conversationId, reply);
            return new AiChatReplyResult(
                    AiChatReplyStatus.ANSWERED,
                    savedMessage,
                    remainingRequests - 1
            );
        } catch (RuntimeException exception) {
            log.warn("AI reply failed for conversation {}", conversationId, exception);
            return new AiChatReplyResult(AiChatReplyStatus.UNAVAILABLE, null, remainingRequests);
        }
    }

    private List<IAiChatClient.PromptMessage> buildPrompt(List<ChatMessageResult> history) {
        List<IAiChatClient.PromptMessage> prompt = new ArrayList<>();
        prompt.add(new IAiChatClient.PromptMessage("system", SYSTEM_PROMPT));

        for (ChatMessageResult message : history) {
            String role = message.senderRole() == MessageSenderRole.USER ? "user" : "assistant";
            prompt.add(new IAiChatClient.PromptMessage(role, message.content()));
        }

        return prompt;
    }
}
