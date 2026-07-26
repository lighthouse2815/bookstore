package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.AiChatReplyResult;
import java.util.UUID;

public interface IAiChatService {

    AiChatReplyResult requestReply(UUID userId, UUID conversationId);
}
