package com.bookstore.bookstore.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtServiceTest {

    @Test
    void generateAccessToken_roundTripsWithJwtDecoder() {
        String secret = "01234567890123456789012345678901";
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
        JwtService jwtService = new JwtService(encoder, new JwtProperties(secret, 60));
        User user = user();

        String token = jwtService.generateAccessToken(user);
        Jwt jwt = decoder.decode(token);

        assertEquals(user.getId().toString(), jwt.getSubject());
        assertEquals("username", jwt.getClaimAsString("username"));
        assertEquals(List.of("USER"), jwt.getClaimAsStringList("roles"));
        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());
        assertTrue(jwt.getExpiresAt().isAfter(jwt.getIssuedAt()));
    }

    private static User user() {
        Instant now = Instant.now();
        return new User(
                UUID.randomUUID(),
                "username",
                "password-hash",
                "0123456789",
                "test@gmail.com",
                UserStatus.ACTIVE,
                false,
                Set.of(new Role(
                        UUID.randomUUID(),
                        "USER",
                        "Default user role",
                        Set.of(),
                        now,
                        now,
                        null
                )),
                now,
                now,
                null
        );
    }
}
