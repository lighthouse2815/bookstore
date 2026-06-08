package com.bookstore.bookstore.infrastructure.security;

import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final IUserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userId = parseUserId(jwt.getSubject());

        User user = userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> unauthorized("User not found"));

        try {
            user.requireCanLogin();
        } catch (DomainException exception) {
            throw unauthorized("User is not allowed to use this token");
        }

        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getName)
                .filter(Objects::nonNull)
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        return new JwtAuthenticationToken(jwt, authorities);
    }

    private UUID parseUserId(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (Exception exception) {
            throw unauthorized("Invalid token subject");
        }
    }

    private OAuth2AuthenticationException unauthorized(String message) {
        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token", message, null)
        );
    }
}
