package com.bookstore.bookstore.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthSecurityProperties(
        Login login,
        Reset reset,
        Web web,
        TrustedProxy trustedProxy
) {
    public record Login(
            int maxFailuresPerAccount,
            int maxFailuresPerIp,
            long windowMinutes,
            long lockMinutes
    ) {}

    public record Reset(
            int maxRequestsPerEmail,
            int maxRequestsPerIp,
            long windowMinutes,
            long lockMinutes
    ) {}

    public record Web(
            boolean cookieSecure,
            String cookieSameSite,
            long cookieMaxAgeSeconds
    ) {}

    public record TrustedProxy(
            boolean enabled,
            java.util.List<String> cidrs
    ) {}
}
