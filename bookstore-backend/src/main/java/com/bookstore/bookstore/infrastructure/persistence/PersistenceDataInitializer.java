package com.bookstore.bookstore.infrastructure.persistence;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.infrastructure.persistence.entity.PermissionJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.RoleJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.repository.PermissionJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.RoleJpaRepository;
import java.time.Instant;
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

    private final PermissionJpaRepository permissionJpaRepository;
    private final RoleJpaRepository roleJpaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPermissions();
        seedRoles();
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
                "ADMIN",
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
