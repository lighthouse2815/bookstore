package com.bookstore.bookstore.application.port.out;

import java.util.List;

public interface IAiChatClient {

    boolean isConfigured();

    String generateReply(List<PromptMessage> messages);

    record PromptMessage(String role, String content) {
    }
}
