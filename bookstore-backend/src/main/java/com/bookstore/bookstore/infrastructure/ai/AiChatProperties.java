package com.bookstore.bookstore.infrastructure.ai;

import com.bookstore.bookstore.application.port.out.IAiChatSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.chat")
public record AiChatProperties(
        boolean enabled,
        String baseUrl,
        String accountId,
        String apiToken,
        String model,
        int maxTokens,
        double temperature,
        int historyLimit,
        int dailyUserLimit
) implements IAiChatSettings {
}
