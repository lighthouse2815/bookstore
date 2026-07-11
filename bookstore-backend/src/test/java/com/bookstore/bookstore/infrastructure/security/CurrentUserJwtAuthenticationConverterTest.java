package com.bookstore.bookstore.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.port.out.IRefreshTokenRepository;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class CurrentUserJwtAuthenticationConverterTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private CurrentUserJwtAuthenticationConverter converter;

    @Test
    void convert_activeUserLoadsRolesFromDatabase() {
        User user = user(UserStatus.ACTIVE, false, null, "USER", "ADMIN");
        Jwt jwt = jwt(user.getId().toString());
        when(userRepository.findByIdIncludingDeleted(user.getId())).thenReturn(Optional.of(user));

        Authentication authentication = converter.convert(jwt);

        assertEquals(2, authentication.getAuthorities().size());
        assertEquals(
                Set.of("ROLE_USER", "ROLE_ADMIN"),
                authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    void convert_lockedUserRejectsToken() {
        User user = user(UserStatus.ACTIVE, true, null, "USER");
        Jwt jwt = jwt(user.getId().toString());
        when(userRepository.findByIdIncludingDeleted(user.getId())).thenReturn(Optional.of(user));

        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> converter.convert(jwt)
        );

        assertEquals("invalid_token", exception.getError().getErrorCode());
    }

    @Test
    void convert_invalidSubjectRejectsToken() {
        OAuth2AuthenticationException exception = assertThrows(
                OAuth2AuthenticationException.class,
                () -> converter.convert(jwt("not-a-uuid"))
        );

        assertEquals("invalid_token", exception.getError().getErrorCode());
    }

    private static Jwt jwt(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("roles", Set.of("USER"))
                .build();
    }

    private static User user(
            UserStatus status,
            boolean locked,
            Instant deletedAt,
            String... roleNames
    ) {
        Instant now = Instant.now();
        return new User(
                UUID.randomUUID(),
                "username",
                "password-hash",
                "0123456789",
                "test@gmail.com",
                status,
                locked,
                java.util.Arrays.stream(roleNames)
                        .map(roleName -> new Role(
                                UUID.randomUUID(),
                                roleName,
                                roleName + " role",
                                Set.of(),
                                now,
                                now,
                                null
                        ))
                        .collect(java.util.stream.Collectors.toSet()),
                now,
                now,
                deletedAt
        );
    }
}
