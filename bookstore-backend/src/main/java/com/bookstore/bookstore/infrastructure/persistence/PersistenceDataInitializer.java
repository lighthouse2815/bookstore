package com.bookstore.bookstore.infrastructure.persistence;

import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.domain.enums.Gender;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.PermissionJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ProfileJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.RoleJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.repository.PermissionJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.ProfileJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.RoleJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(0)
@RequiredArgsConstructor
public class PersistenceDataInitializer implements ApplicationRunner {

    private static final String ADMIN_ROLE = "ADMIN";

    private final PermissionJpaRepository permissionJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProfileJpaRepository profileJpaRepository;
    private final IPasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPermissions();
        seedRoles();
        seedAdminAccount();
    }

    private void seedPermissions() {
        for (PermissionCode code : PermissionCode.values()) {
            if (permissionJpaRepository.findByCode(code).isPresent()) {
                continue;
            }

            Instant now = Instant.now();
            PermissionJpaEntity entity = new PermissionJpaEntity();
            entity.setId(UUID.randomUUID());
            entity.setCode(code);
            entity.setDescription(toDescription(code));
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            entity.setDeletedAt(null);
            permissionJpaRepository.save(entity);
        }
    }

    private void seedRoles() {
        seedRole(
                ADMIN_ROLE,
                "Default admin role",
                EnumSet.allOf(PermissionCode.class)
        );
        seedRole(
                "STAFF",
                "Default staff role",
                EnumSet.of(
                        PermissionCode.BOOK_VIEW,
                        PermissionCode.BOOK_CREATE,
                        PermissionCode.BOOK_UPDATE,
                        PermissionCode.BOOK_DELETE,
                        PermissionCode.CATEGORY_VIEW,
                        PermissionCode.CATEGORY_CREATE,
                        PermissionCode.CATEGORY_UPDATE,
                        PermissionCode.CATEGORY_DELETE,
                        PermissionCode.AUTHOR_VIEW,
                        PermissionCode.AUTHOR_CREATE,
                        PermissionCode.AUTHOR_UPDATE,
                        PermissionCode.AUTHOR_DELETE,
                        PermissionCode.PUBLISHER_VIEW,
                        PermissionCode.PUBLISHER_CREATE,
                        PermissionCode.PUBLISHER_UPDATE,
                        PermissionCode.PUBLISHER_DELETE,
                        PermissionCode.ORDER_VIEW_ALL,
                        PermissionCode.ORDER_UPDATE_STATUS,
                        PermissionCode.USER_VIEW,
                        PermissionCode.ROLE_VIEW,
                        PermissionCode.REVIEW_MANAGE,
                        PermissionCode.COUPON_MANAGE,
                        PermissionCode.DASHBOARD_VIEW
                )
        );
        seedRole(
                "USER",
                "Default user role",
                EnumSet.of(
                        PermissionCode.BOOK_VIEW,
                        PermissionCode.CATEGORY_VIEW,
                        PermissionCode.AUTHOR_VIEW,
                        PermissionCode.PUBLISHER_VIEW,
                        PermissionCode.ORDER_CREATE,
                        PermissionCode.ORDER_VIEW_OWN,
                        PermissionCode.ORDER_CANCEL_OWN,
                        PermissionCode.REVIEW_CREATE
                )
        );
        seedRole(
                "SHIPPER",
                "Default shipper role",
                EnumSet.of(
                        PermissionCode.SHIPMENT_VIEW_OWN,
                        PermissionCode.SHIPMENT_UPDATE_OWN
                )
        );
    }

    private void seedRole(String name, String description, Set<PermissionCode> permissionCodes) {
        if (roleJpaRepository.existsByName(name)) {
            return;
        }

        Instant now = Instant.now();
        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(name);
        entity.setDescription(description);
        entity.setPermissions(permissionCodes.stream()
                .map(code -> permissionJpaRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalStateException("Permission not found: " + code)))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(null);
        roleJpaRepository.save(entity);
    }

    private void seedAdminAccount() {
        if (!isAdminSeedEnabled() || hasActiveAdminUser()) {
            return;
        }

        RoleJpaEntity adminRole = roleJpaRepository.findByNameAndDeletedAtIsNull(ADMIN_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + ADMIN_ROLE));

        String adminUsername = requiredAdminProperty("app.admin.username");
        String adminPassword = requiredAdminProperty("app.admin.password");
        String adminPhone = requiredAdminProperty("app.admin.phone");
        String adminEmail = requiredAdminProperty("app.admin.email");
        String adminLastName = requiredAdminProperty("app.admin.last-name");
        String adminFirstName = requiredAdminProperty("app.admin.first-name");

        rejectConflictingAdminIdentity(adminUsername, adminEmail);

        UserJpaEntity adminUser = createAdminUser(adminRole, adminUsername, adminPassword, adminPhone, adminEmail);
        createAdminProfile(adminUser, adminLastName, adminFirstName);
    }

    private UserJpaEntity createAdminUser(
            RoleJpaEntity adminRole,
            String adminUsername,
            String adminPassword,
            String adminPhone,
            String adminEmail
    ) {
        Instant now = Instant.now();
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(adminUsername);
        entity.setPasswordHash(passwordEncoder.encode(adminPassword));
        entity.setPhoneNumber(adminPhone);
        entity.setEmail(adminEmail);
        entity.setStatus(UserStatus.ACTIVE);
        entity.setLocked(false);
        entity.setRoles(new LinkedHashSet<>(Set.of(adminRole)));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(null);
        return userJpaRepository.save(entity);
    }

    private boolean isAdminSeedEnabled() {
        return environment.getProperty("app.admin.seed-enabled", Boolean.class, false);
    }

    private boolean hasActiveAdminUser() {
        return userJpaRepository.findPageIdsByRoleNameActive(ADMIN_ROLE, PageRequest.of(0, 1)).hasContent();
    }

    private void rejectConflictingAdminIdentity(String adminUsername, String adminEmail) {
        if (userJpaRepository.findByUsername(adminUsername).isPresent()) {
            throw new IllegalStateException("Configured admin username already exists: " + adminUsername);
        }

        if (userJpaRepository.findByEmail(adminEmail).isPresent()) {
            throw new IllegalStateException("Configured admin email already exists: " + adminEmail);
        }
    }

    private String requiredAdminProperty(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required admin seed config: " + key);
        }

        return value.trim();
    }

    private void createAdminProfile(UserJpaEntity adminUser, String adminLastName, String adminFirstName) {
        Instant now = Instant.now();
        ProfileJpaEntity entity = new ProfileJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUser(adminUser);
        entity.setLastName(adminLastName);
        entity.setFirstName(adminFirstName);
        entity.setAvatarUrl(null);
        entity.setGender(Gender.MALE);
        entity.setDateOfBirth(LocalDate.of(1990, 1, 1));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(null);
        profileJpaRepository.save(entity);
    }

    private String toDescription(PermissionCode code) {
        return Arrays.stream(code.name().split("_"))
                .map(this::capitalize)
                .collect(Collectors.joining(" "));
    }

    private String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }

        String lower = value.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
