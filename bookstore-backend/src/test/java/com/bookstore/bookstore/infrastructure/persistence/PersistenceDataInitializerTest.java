package com.bookstore.bookstore.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.infrastructure.persistence.repository.ProfileJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.admin.seed-enabled=true",
        "app.admin.username=test-admin",
        "app.admin.password=TestAdmin@12345",
        "app.admin.phone=0909999999",
        "app.admin.email=test-admin@example.com",
        "app.admin.last-name=Test",
        "app.admin.first-name=Admin"
})
class PersistenceDataInitializerTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ProfileJpaRepository profileJpaRepository;

    @Test
    void run_seedsDefaultAdminUserAndProfile() {
        var adminUser = userJpaRepository.findByUsername("test-admin")
                .orElseThrow();

        assertEquals(UserStatus.ACTIVE, adminUser.getStatus());
        assertFalse(adminUser.isLocked());
        assertTrue(adminUser.getDeletedAt() == null);
        assertTrue(adminUser.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName())));

        var profile = profileJpaRepository.findByUserId(adminUser.getId())
                .orElseThrow();

        assertEquals(adminUser.getId(), profile.getUser().getId());
        assertEquals("Test", profile.getLastName());
        assertEquals("Admin", profile.getFirstName());
    }
}
