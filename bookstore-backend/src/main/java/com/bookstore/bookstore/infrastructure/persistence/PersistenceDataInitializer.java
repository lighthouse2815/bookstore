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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PersistenceDataInitializer implements ApplicationRunner {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ADMIN_USERNAME = "giamdocdang";
    private static final String ADMIN_PASSWORD = "123123aa";
    private static final String ADMIN_PHONE = "0900000001";
    private static final String ADMIN_EMAIL = "giamdocdang@gmail.com";
    private static final String ADMIN_LAST_NAME = "Dang";
    private static final String ADMIN_FIRST_NAME = "Giam Doc";

    private final PermissionJpaRepository permissionJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProfileJpaRepository profileJpaRepository;
    private final IPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPermissions();
        seedRoles();
        seedAdminAccount();
    }

    private void seedPermissions() {
        for (PermissionCode code : PermissionCode.values()) {
            if (permissionJpaRepository.findByCodeIncludingDeleted(code).isPresent()) {
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
    }

    private void seedRole(String name, String description, Set<PermissionCode> permissionCodes) {
        if (roleJpaRepository.existsByNameIncludingDeleted(name)) {
            return;
        }

        Instant now = Instant.now();
        RoleJpaEntity entity = new RoleJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(name);
        entity.setDescription(description);
        entity.setPermissions(permissionCodes.stream()
                .map(code -> permissionJpaRepository.findByCodeIncludingDeleted(code)
                        .orElseThrow(() -> new IllegalStateException("Permission not found: " + code)))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(null);
        roleJpaRepository.save(entity);
    }

    private void seedAdminAccount() {
        RoleJpaEntity adminRole = roleJpaRepository.findByNameActive(ADMIN_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + ADMIN_ROLE));

        UserJpaEntity adminUser = userJpaRepository.findByUsernameIncludingDeleted(ADMIN_USERNAME)
                .map(existingUser -> ensureAdminUser(existingUser, adminRole))
                .orElseGet(() -> createAdminUser(adminRole));

        ensureAdminProfile(adminUser);
    }

    private UserJpaEntity ensureAdminUser(UserJpaEntity user, RoleJpaEntity adminRole) {
        Instant now = Instant.now();
        user.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setLocked(false);
        user.setUpdatedAt(now);
        user.setDeletedAt(null);

        if (user.getRoles().stream().noneMatch(role -> ADMIN_ROLE.equals(role.getName()))) {
            user.getRoles().add(adminRole);
        }

        return userJpaRepository.save(user);
    }

    private UserJpaEntity createAdminUser(RoleJpaEntity adminRole) {
        Instant now = Instant.now();
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(ADMIN_USERNAME);
        entity.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        entity.setPhoneNumber(ADMIN_PHONE);
        entity.setEmail(ADMIN_EMAIL);
        entity.setStatus(UserStatus.ACTIVE);
        entity.setLocked(false);
        entity.setRoles(new LinkedHashSet<>(Set.of(adminRole)));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(null);
        return userJpaRepository.save(entity);
    }

    private void ensureAdminProfile(UserJpaEntity adminUser) {
        profileJpaRepository.findByUserIdIncludingDeleted(adminUser.getId())
                .ifPresentOrElse(
                        existingProfile -> restoreProfileIfNeeded(existingProfile),
                        () -> createAdminProfile(adminUser)
                );
    }

    private void restoreProfileIfNeeded(ProfileJpaEntity profile) {
        if (profile.getDeletedAt() == null) {
            return;
        }

        Instant now = Instant.now();
        profile.setUpdatedAt(now);
        profile.setDeletedAt(null);
        profileJpaRepository.save(profile);
    }

    private void createAdminProfile(UserJpaEntity adminUser) {
        Instant now = Instant.now();
        ProfileJpaEntity entity = new ProfileJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUser(adminUser);
        entity.setLastName(ADMIN_LAST_NAME);
        entity.setFirstName(ADMIN_FIRST_NAME);
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
