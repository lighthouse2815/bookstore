package com.bookstore.bookstore.infrastructure.security;

import com.bookstore.bookstore.application.port.out.IJwtService;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService implements IJwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generateAccessToken(User user) {
        return generateAccessToken(user, null);
    }

    @Override
    public String generateAccessToken(User user, java.util.UUID sessionId) {
        Instant now = Instant.now();

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES))
                .claim("username", user.getUsername())
                .claim("roles", roles);
        if (sessionId != null) {
            claimsBuilder.claim("sid", sessionId.toString());
        }
        JwtClaimsSet claims = claimsBuilder.build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public Instant calculateRefreshTokenExpiresAt(Instant issuedAt) {
        return issuedAt.plus(jwtProperties.refreshExpirationDays(), ChronoUnit.DAYS);
    }
}
