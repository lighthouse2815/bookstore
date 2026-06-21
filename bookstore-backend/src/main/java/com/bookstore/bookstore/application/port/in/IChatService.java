package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.AssignConversationCommand;
import com.bookstore.bookstore.application.command.CreateConversationCommand;
import com.bookstore.bookstore.application.command.SendChatMessageCommand;
import com.bookstore.bookstore.application.result.ChatMessageResult;
import com.bookstore.bookstore.application.result.ChatMessageSliceResult;
import com.bookstore.bookstore.application.result.ConversationResult;
import com.bookstore.bookstore.application.result.ConversationSliceResult;
import com.bookstore.bookstore.domain.enums.ConversationStatus;
import java.util.List;
import java.util.UUID;

public interface IChatService {

    List<ConversationResult> getMyConversations(UUID userId);

    ConversationResult createConversation(CreateConversationCommand command);

    ConversationResult getConversationDetail(UUID userId, UUID conversationId);

    ChatMessageSliceResult getMessages(UUID userId, UUID conversationId, int page, int size);

    ChatMessageResult sendUserMessage(SendChatMessageCommand command);

    ConversationResult markConversationRead(UUID userId, UUID conversationId);

    ConversationResult closeMyConversation(UUID userId, UUID conversationId);

    ConversationSliceResult getAdminConversations(UUID adminUserId, ConversationStatus status, String keyword, int page, int size);

    ConversationResult getAdminConversationDetail(UUID adminUserId, UUID conversationId);

    ChatMessageSliceResult getAdminMessages(UUID adminUserId, UUID conversationId, int page, int size);

    ChatMessageResult sendAdminMessage(SendChatMessageCommand command);

    ConversationResult assignConversation(AssignConversationCommand command);

    ConversationResult markAdminConversationRead(UUID adminUserId, UUID conversationId);

    ConversationResult closeConversation(UUID adminUserId, UUID conversationId);

    ConversationResult reopenConversation(UUID adminUserId, UUID conversationId);
}
