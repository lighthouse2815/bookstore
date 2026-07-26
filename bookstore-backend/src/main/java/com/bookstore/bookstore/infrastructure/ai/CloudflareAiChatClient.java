package com.bookstore.bookstore.infrastructure.ai;

import com.bookstore.bookstore.application.port.out.IAiChatClient;
import com.bookstore.bookstore.shared.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CloudflareAiChatClient implements IAiChatClient {

    private final RestClient restClient;
    private final AiChatProperties properties;

    public CloudflareAiChatClient(RestClient.Builder restClientBuilder, AiChatProperties properties) {
        this.properties = properties;
        String baseUrl = StringUtils.trimToNull(properties.baseUrl());
        this.restClient = baseUrl == null
                ? restClientBuilder.build()
                : restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.trimToNull(properties.baseUrl()) != null
                && StringUtils.trimToNull(properties.accountId()) != null
                && StringUtils.trimToNull(properties.apiToken()) != null
                && StringUtils.trimToNull(properties.model()) != null;
    }

    @Override
    public String generateReply(List<PromptMessage> messages) {
        if (!isConfigured()) {
            throw new IllegalStateException("Cloudflare Workers AI is not configured");
        }

        CloudflareChatResponse response = restClient.post()
                .uri("/accounts/{accountId}/ai/v1/chat/completions", properties.accountId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiToken())
                .body(new CloudflareChatRequest(
                        properties.model(),
                        messages.stream()
                                .map(message -> new CloudflareMessage(message.role(), message.content()))
                                .toList(),
                        Math.max(64, properties.maxTokens()),
                        properties.temperature()
                ))
                .retrieve()
                .body(CloudflareChatResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Cloudflare Workers AI returned no choices");
        }

        String content = StringUtils.trimToNull(response.choices().getFirst().message().content());
        if (content == null) {
            throw new IllegalStateException("Cloudflare Workers AI returned an empty response");
        }
        return content;
    }

    private record CloudflareChatRequest(
            String model,
            List<CloudflareMessage> messages,
            @JsonProperty("max_tokens") int maxTokens,
            double temperature
    ) {
    }

    private record CloudflareMessage(String role, String content) {
    }

    private record CloudflareChatResponse(List<CloudflareChoice> choices) {
    }

    private record CloudflareChoice(CloudflareMessage message) {
    }
}
