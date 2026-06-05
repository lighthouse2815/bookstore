package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.enums.RoleName;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.domain.model.Role;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoleServiceTest {

    private PermissionService permissionService;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();
        roleService = new RoleService(permissionService);
    }

    @Test
    void getAll_returnsSeededRoles() {
        assertEquals(3, roleService.getAll().size());
    }

    @Test
    void getByName_returnsSeededRole() {
        Role role = roleService.getByName(RoleName.USER);

        assertEquals(RoleName.USER, role.getName());
        assertTrue(roleService.hasPermission(RoleName.USER, PermissionCode.BOOK_VIEW));
    }

    @Test
    void create_rejectsDuplicateName() {
        Role current = roleService.getByName(RoleName.USER);
        Role duplicate = new Role(
                UUID.randomUUID(),
                current.getName(),
                "duplicate",
                current.getPermissions(),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> roleService.create(duplicate)
        );

        assertEquals(ApplicationErrorCode.ROLE_NAME_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void update_replacesDescription() {
        Role current = roleService.getByName(RoleName.USER);
        Set<Permission> permissions = new LinkedHashSet<>(current.getPermissions());
        Role updated = new Role(
                current.getId(),
                current.getName(),
                "updated description",
                permissions,
                current.getCreatedAt(),
                current.getUpdatedAt(),
                current.getDeletedAt()
        );

        Role saved = roleService.update(updated);

        assertEquals("updated description", saved.getDescription());
        assertEquals("updated description", roleService.getByName(RoleName.USER).getDescription());
    }

    @Test
    void delete_removesRole() {
        Role current = roleService.getByName(RoleName.USER);

        roleService.delete(current.getId());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> roleService.getByName(RoleName.USER)
        );

        assertEquals(ApplicationErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());
    }
}
