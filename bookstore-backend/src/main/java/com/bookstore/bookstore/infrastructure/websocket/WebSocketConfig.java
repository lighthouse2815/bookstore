package com.bookstore.bookstore.infrastructure.websocket;

import com.bookstore.bookstore.infrastructure.security.CorsProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CorsProperties corsProperties;
    private final StompJwtChannelInterceptor stompJwtChannelInterceptor;

    public WebSocketConfig(
            CorsProperties corsProperties,
            StompJwtChannelInterceptor stompJwtChannelInterceptor
    ) {
        this.corsProperties = corsProperties;
        this.stompJwtChannelInterceptor = stompJwtChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowedOrigins = corsProperties.allowedOrigins() == null
                ? new String[0]
                : corsProperties.allowedOrigins().toArray(String[]::new);

        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtChannelInterceptor);
    }
}
