package com.bookstore.bookstore.config;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProdRuntimeSettingsValidator {

    private static final int MIN_JWT_SECRET_LENGTH = 32;
    private static final int MIN_SEED_PASSWORD_LENGTH = 8;
    private static final Set<String> REJECTED_PLACEHOLDER_VALUES = Set.of(
            "01234567890123456789012345678901",
            "ProdAdmin@12345",
            "SeedUser@12345",
            "change_me_admin_password",
            "change_me_demo_seed_password",
            "change_me_dev_only",
            "change_me_seed_only",
            "replace_with_a_32_char_minimum_secret"
    );

    private final Environment environment;

    @PostConstruct
    void validate() {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }

        requireNonBlank("DB_HOST", "Thiếu biến môi trường production: DB_HOST");
        requireNonBlank("DB_PORT", "Thiếu biến môi trường production: DB_PORT");
        requireNonBlank("DB_NAME", "Thiếu biến môi trường production: DB_NAME");
        requireNonBlank("DB_USER", "Thiếu biến môi trường production: DB_USER");
        requireNonBlank("DB_PASSWORD", "Thiếu biến môi trường production: DB_PASSWORD");

        String jwtSecret = requireNonBlank("app.jwt.secret", "Thiếu biến môi trường production: JWT_SECRET");
        if (jwtSecret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException("JWT_SECRET phải có ít nhất 32 ký tự khi chạy production");
        }
        rejectPlaceholderValue(jwtSecret, "JWT_SECRET");

        String allowedOrigins = requireNonBlank(
                "app.cors.allowed-origins",
                "Thiếu biến môi trường production: CORS_ALLOWED_ORIGINS"
        );
        if (containsWildcardOrigin(allowedOrigins)) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS không được chứa '*' khi chạy production");
        }

        if (!environment.getProperty("app.auth.web.cookie-secure", Boolean.class, false)) {
            throw new IllegalStateException("AUTH_WEB_COOKIE_SECURE phải=true khi chạy production HTTPS");
        }
        String sameSite = requireNonBlank("app.auth.web.cookie-same-site", "Thiếu biến production: AUTH_WEB_COOKIE_SAME_SITE");
        if (!Set.of("Lax", "Strict", "None").contains(sameSite)) {
            throw new IllegalStateException("AUTH_WEB_COOKIE_SAME_SITE chỉ nhận Lax, Strict hoặc None");
        }
        requireNonBlank("app.google.client-id", "Thiếu biến production: GOOGLE_CLIENT_ID");
        String webhookApiKey = environment.getProperty("app.sepay.webhook-api-key");
        String secretKey = environment.getProperty("app.sepay.secret-key");
        if ((webhookApiKey == null || webhookApiKey.isBlank()) && (secretKey == null || secretKey.isBlank())) {
            throw new IllegalStateException("Thiếu biến production: SEPAY_WEBHOOK_API_KEY hoặc SEPAY_SECRET_KEY");
        }
        if (environment.getProperty("app.auth.trusted-proxy.enabled", Boolean.class, false)) {
            requireNonBlank("app.auth.trusted-proxy.cidrs", "Thiếu biến production: AUTH_TRUSTED_PROXY_CIDRS");
        }

        if (environment.getProperty("app.admin.seed-enabled", Boolean.class, false)) {
            requireNonBlank("app.admin.username", "Thiếu cấu hình production khi APP_ADMIN_SEED_ENABLED=true: ADMIN_USERNAME");
            String adminPassword = requireNonBlank(
                    "app.admin.password",
                    "Thiếu cấu hình production khi APP_ADMIN_SEED_ENABLED=true: ADMIN_PASSWORD"
            );
            requireNonBlank("app.admin.phone", "Thiếu cấu hình production khi APP_ADMIN_SEED_ENABLED=true: ADMIN_PHONE");
            requireNonBlank("app.admin.email", "Thiếu cấu hình production khi APP_ADMIN_SEED_ENABLED=true: ADMIN_EMAIL");
            requireNonBlank(
                    "app.admin.last-name",
                    "Thiếu cấu hình production khi APP_ADMIN_SEED_ENABLED=true: ADMIN_LAST_NAME"
            );
            requireNonBlank(
                    "app.admin.first-name",
                    "Thiếu cấu hình production khi APP_ADMIN_SEED_ENABLED=true: ADMIN_FIRST_NAME"
            );
            rejectShortOrPlaceholderPassword(adminPassword, "ADMIN_PASSWORD");
        }

        if (environment.getProperty("app.seed.enabled", Boolean.class, false)) {
            String demoPassword = requireNonBlank(
                    "app.seed.default-password",
                    "Thiếu cấu hình production khi APP_DEMO_SEED_ENABLED=true: APP_DEMO_USER_PASSWORD"
            );
            rejectShortOrPlaceholderPassword(demoPassword, "APP_DEMO_USER_PASSWORD");
        }
    }

    private String requireNonBlank(String key, String message) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
        return value.trim();
    }

    private void rejectShortOrPlaceholderPassword(String password, String key) {
        if (password.length() < MIN_SEED_PASSWORD_LENGTH) {
            throw new IllegalStateException(key + " phải có ít nhất 8 ký tự khi chạy production");
        }
        rejectPlaceholderValue(password, key);
    }

    private void rejectPlaceholderValue(String value, String key) {
        if (REJECTED_PLACEHOLDER_VALUES.contains(value)) {
            throw new IllegalStateException(key + " đang dùng giá trị placeholder, hãy thay bằng giá trị riêng cho môi trường production");
        }
    }

    private boolean containsWildcardOrigin(String allowedOrigins) {
        return List.of(allowedOrigins.split(",")).stream()
                .map(String::trim)
                .anyMatch(origin -> origin.equals("*") || origin.contains("*"));
    }
}
