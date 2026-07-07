package com.bookstore.bookstore.infrastructure.websocket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class StompJwtChannelInterceptorTest {

    private StompJwtChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new StompJwtChannelInterceptor(
                mock(JwtDecoder.class),
                mock(CurrentUserJwtAuthenticationConverter.class)
        );
    }

    @Test
    void preSend_whenCustomerSubscribesToAdminChatTopic_rejects() {
        Message<byte[]> message = message(
                StompCommand.SUBSCRIBE,
                "/topic/admin/chat/conversations",
                authentication("ROLE_USER")
        );

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_whenAdminSubscribesToAdminChatTopic_allows() {
        Message<byte[]> message = message(
                StompCommand.SUBSCRIBE,
                "/topic/admin/chat/conversations",
                authentication("ROLE_ADMIN")
        );

        assertDoesNotThrow(() -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_whenStaffSubscribesToAdminChatTopic_allows() {
        Message<byte[]> message = message(
                StompCommand.SUBSCRIBE,
                "/topic/admin/chat/conversations",
                authentication("ROLE_STAFF")
        );

        assertDoesNotThrow(() -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_whenCustomerSendsToAdminChatTopic_rejects() {
        Message<byte[]> message = message(
                StompCommand.SEND,
                "/topic/admin/chat/conversations",
                authentication("ROLE_USER")
        );

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void preSend_whenCustomerSubscribesToOwnQueue_allows() {
        Message<byte[]> message = message(
                StompCommand.SUBSCRIBE,
                "/user/queue/chat/messages",
                authentication("ROLE_USER")
        );

        assertDoesNotThrow(() -> interceptor.preSend(message, null));
    }

    private static Message<byte[]> message(
            StompCommand command,
            String destination,
            UsernamePasswordAuthenticationToken authentication
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("session-1");
        accessor.setDestination(destination);
        accessor.setUser(authentication);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static UsernamePasswordAuthenticationToken authentication(String authority) {
        return new UsernamePasswordAuthenticationToken(
                "user",
                "n/a",
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
