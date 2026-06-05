package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PermissionServiceTest {

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();
    }

    @Test
    void getAll_returnsSeededPermissions() {
        assertEquals(PermissionCode.values().length, permissionService.getAll().size());
    }

    @Test
    void getByCode_returnsSeededPermission() {
        Permission permission = permissionService.getByCode(PermissionCode.BOOK_VIEW);

        assertEquals(PermissionCode.BOOK_VIEW, permission.getCode());
    }

    @Test
    void create_rejectsDuplicateCode() {
        Permission permission = new Permission(
                UUID.randomUUID(),
                PermissionCode.BOOK_VIEW,
                "book view duplicate",
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> permissionService.create(permission)
        );

        assertEquals(ApplicationErrorCode.PERMISSION_CODE_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void update_replacesDescription() {
        Permission current = permissionService.getByCode(PermissionCode.BOOK_VIEW);
        Permission updated = new Permission(
                current.getId(),
                current.getCode(),
                "updated description",
                current.getCreatedAt(),
                current.getUpdatedAt(),
                current.getDeletedAt()
        );

        Permission saved = permissionService.update(updated);

        assertEquals("updated description", saved.getDescription());
        assertEquals("updated description", permissionService.getByCode(PermissionCode.BOOK_VIEW).getDescription());
    }

    @Test
    void delete_removesPermission() {
        Permission permission = permissionService.getByCode(PermissionCode.BOOK_VIEW);

        permissionService.delete(permission.getId());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> permissionService.getByCode(PermissionCode.BOOK_VIEW)
        );

        assertEquals(ApplicationErrorCode.PERMISSION_NOT_FOUND, exception.getErrorCode());
    }
}
