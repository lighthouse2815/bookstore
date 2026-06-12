package com.bookstore.bookstore.infrastructure.security;

import com.bookstore.bookstore.application.exception.GoogleIdTokenVerificationException;
import com.bookstore.bookstore.application.port.out.IGoogleIdTokenVerifier;
import com.bookstore.bookstore.application.port.out.VerifiedGoogleIdToken;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class GoogleIdTokenVerifierAdapter implements IGoogleIdTokenVerifier {

    private static final String GOOGLE_JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String GOOGLE_ISSUER = "https://accounts.google.com";
    private static final String GOOGLE_LEGACY_ISSUER = "accounts.google.com";
    private static final OAuth2Error INVALID_ISSUER_ERROR = new OAuth2Error(
            "invalid_token",
            "Google token issuer is invalid",
            null
    );
    private static final OAuth2Error INVALID_AUDIENCE_ERROR = new OAuth2Error(
            "invalid_token",
            "Google token audience is invalid",
            null
    );

    private final GoogleAuthProperties googleAuthProperties;
    private final JwtDecoder jwtDecoder;

    public GoogleIdTokenVerifierAdapter(GoogleAuthProperties googleAuthProperties) {
        this.googleAuthProperties = googleAuthProperties;

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWK_SET_URI).build();
        decoder.setJwtValidator(token -> {
            OAuth2TokenValidatorResult timestampResult = new JwtTimestampValidator().validate(token);
            if (timestampResult.hasErrors()) {
                return timestampResult;
            }

            OAuth2TokenValidatorResult issuerResult = validateIssuer(token);
            if (issuerResult.hasErrors()) {
                return issuerResult;
            }

            return validateAudience(token);
        });
        this.jwtDecoder = decoder;
    }

    @Override
    public VerifiedGoogleIdToken verify(String idToken) {
        if (StringUtils.isBlank(idToken)) {
            throw new GoogleIdTokenVerificationException("Google ID token is blank");
        }

        if (StringUtils.isBlank(googleAuthProperties.clientId())) {
            throw new IllegalStateException("Google Sign-In is not configured");
        }

        try {
            Jwt jwt = jwtDecoder.decode(idToken);
            String subject = StringUtils.trimToNull(jwt.getSubject());
            String email = StringUtils.trimToNull(jwt.getClaimAsString("email"));

            if (subject == null || email == null) {
                throw new GoogleIdTokenVerificationException("Google ID token is missing required claims");
            }

            return new VerifiedGoogleIdToken(
                    subject,
                    email,
                    Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")),
                    StringUtils.trimToNull(jwt.getClaimAsString("given_name")),
                    StringUtils.trimToNull(jwt.getClaimAsString("family_name")),
                    StringUtils.trimToNull(jwt.getClaimAsString("picture"))
            );
        } catch (JwtException exception) {
            throw new GoogleIdTokenVerificationException("Google ID token is invalid", exception);
        }
    }

    private OAuth2TokenValidatorResult validateIssuer(Jwt token) {
        String issuer = token.getIssuer() == null ? null : token.getIssuer().toString();
        if (GOOGLE_ISSUER.equals(issuer) || GOOGLE_LEGACY_ISSUER.equals(issuer)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(INVALID_ISSUER_ERROR);
    }

    private OAuth2TokenValidatorResult validateAudience(Jwt token) {
        List<String> audience = token.getAudience();
        if (audience != null && audience.contains(googleAuthProperties.clientId())) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE_ERROR);
    }
}
