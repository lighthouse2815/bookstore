package com.bookstore.bookstore.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ProdRuntimeSettingsValidatorTest {

    @Test
    void validate_whenProdConfigurationIsComplete_passes() {
        MockEnvironment environment = baseProdEnvironment();

        ProdRuntimeSettingsValidator validator = new ProdRuntimeSettingsValidator(environment);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void validate_whenJwtSecretIsPlaceholder_failsFast() {
        MockEnvironment environment = baseProdEnvironment();
        environment.setProperty("app.jwt.secret", "01234567890123456789012345678901");

        ProdRuntimeSettingsValidator validator = new ProdRuntimeSettingsValidator(environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validate);

        assertEquals(
                "JWT_SECRET đang dùng giá trị placeholder, hãy thay bằng giá trị riêng cho môi trường production",
                exception.getMessage()
        );
    }

    @Test
    void validate_whenDemoSeedEnabledWithoutPassword_failsFast() {
        MockEnvironment environment = baseProdEnvironment();
        environment.setProperty("app.seed.enabled", "true");

        ProdRuntimeSettingsValidator validator = new ProdRuntimeSettingsValidator(environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validate);

        assertEquals(
                "Thiếu cấu hình production khi APP_DEMO_SEED_ENABLED=true: APP_DEMO_USER_PASSWORD",
                exception.getMessage()
        );
    }

    @Test
    void validate_whenAdminSeedEnabledWithoutPassword_failsFast() {
        MockEnvironment environment = baseProdEnvironment();
        environment.setProperty("app.admin.seed-enabled", "true");
        environment.setProperty("app.admin.username", "admin-prod");
        environment.setProperty("app.admin.phone", "0900000001");
        environment.setProperty("app.admin.email", "admin@example.com");
        environment.setProperty("app.admin.last-name", "Nguyen");
        environment.setProperty("app.admin.first-name", "Admin");

        ProdRuntimeSettingsValidator validator = new ProdRuntimeSettingsValidator(environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validate);

        assertEquals(
                "Thiếu cấu hình production khi APP_ADMIN_SEED_ENABLED=true: ADMIN_PASSWORD",
                exception.getMessage()
        );
    }

    private static MockEnvironment baseProdEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty("DB_HOST", "localhost");
        environment.setProperty("DB_PORT", "3306");
        environment.setProperty("DB_NAME", "bookstore_db");
        environment.setProperty("DB_USER", "bookstore_user");
        environment.setProperty("DB_PASSWORD", "StrongDbPassword!234");
        environment.setProperty("app.jwt.secret", "0123456789abcdefghijklmnopqrstuvwxyz");
        environment.setProperty("app.cors.allowed-origins", "https://bookstore.example.com");
        environment.setProperty("app.admin.seed-enabled", "false");
        environment.setProperty("app.seed.enabled", "false");
        return environment;
    }
}
