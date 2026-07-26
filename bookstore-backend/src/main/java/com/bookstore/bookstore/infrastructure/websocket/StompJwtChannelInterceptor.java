package com.bookstore.bookstore.infrastructure.websocket;

import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractBearerToken(accessor.getNativeHeader("Authorization"));
            Jwt jwt = jwtDecoder.decode(token);
            AbstractAuthenticationToken authentication = currentUserJwtAuthenticationConverter.convert(jwt);
            accessor.setUser(authentication);
            return message;
        }

        if (requiresAuthenticatedUser(accessor.getCommand()) && accessor.getUser() == null) {
            throw new AccessDeniedException("Unauthorized websocket session");
        }

        if (requiresAdminChatAccess(accessor) && !hasAdminChatAccess(accessor.getUser())) {
            throw new AccessDeniedException("Admin chat websocket access denied");
        }

        return message;
    }

    private String extractBearerToken(List<String> values) {
        if (values == null || values.isEmpty()) {
            throw new AccessDeniedException("Missing Authorization header");
        }

        String authorization = values.get(0);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AccessDeniedException("Invalid Authorization header");
        }

        return authorization.substring("Bearer ".length()).trim();
    }

    private boolean requiresAuthenticatedUser(StompCommand command) {
        return StompCommand.SUBSCRIBE.equals(command)
                || StompCommand.SEND.equals(command)
                || StompCommand.UNSUBSCRIBE.equals(command);
    }

    private boolean requiresAdminChatAccess(StompHeaderAccessor accessor) {
        if (!StompCommand.SUBSCRIBE.equals(accessor.getCommand()) && !StompCommand.SEND.equals(accessor.getCommand())) {
            return false;
        }

        String destination = accessor.getDestination();
        return destination != null && destination.startsWith("/topic/admin/chat/");
    }

    private boolean hasAdminChatAccess(java.security.Principal principal) {
        if (!(principal instanceof Authentication authentication)) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority) || "ROLE_STAFF".equals(authority));
    }
}
