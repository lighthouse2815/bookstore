package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.application.command.CreateRoleCommand;
import com.bookstore.bookstore.application.command.DeleteRoleCommand;
import com.bookstore.bookstore.application.command.UpdateRoleCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.infrastructure.persistence.adapter.InMemoryPermissionRepositoryAdapter;
import com.bookstore.bookstore.infrastructure.persistence.adapter.InMemoryRoleRepositoryAdapter;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoleServiceTest {

    private static final String USER_ROLE = "USER";

    private InMemoryPermissionRepositoryAdapter permissionRepository;
    private InMemoryRoleRepositoryAdapter roleRepository;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        permissionRepository = new InMemoryPermissionRepositoryAdapter();
        roleRepository = new InMemoryRoleRepositoryAdapter(permissionRepository);
        roleService = new RoleService(roleRepository, permissionRepository);
    }

    @Test
    void getAll_returnsSeededRoles() {
        assertEquals(3, roleService.getAll().size());
    }

    @Test
    void repository_containsSeededRole() {
        Role role = roleRepository.findByName(USER_ROLE).orElseThrow();

        assertEquals(USER_ROLE, role.getName());
        assertTrue(role.getPermissions().stream()
                .anyMatch(permission -> permission.getCode() == PermissionCode.BOOK_VIEW));
    }

    @Test
    void create_rejectsDuplicateName() {
        Role current = roleRepository.findByName(USER_ROLE).orElseThrow();
        CreateRoleCommand duplicate = new CreateRoleCommand(
                current.getName(),
                "duplicate",
                current.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> roleService.create(duplicate)
        );

        assertEquals(ApplicationErrorCode.ROLE_NAME_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void update_replacesDescription() {
        Role current = roleRepository.findByName(USER_ROLE).orElseThrow();
        UpdateRoleCommand updated = new UpdateRoleCommand(
                current.getId(),
                current.getName(),
                "updated description",
                current.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
        );

        Role saved = roleService.update(updated);

        assertEquals("updated description", saved.getDescription());
        assertEquals("updated description", roleRepository.findByName(USER_ROLE).orElseThrow().getDescription());
    }

    @Test
    void delete_removesRole() {
        Role current = roleRepository.findByName(USER_ROLE).orElseThrow();

        roleService.delete(new DeleteRoleCommand(current.getId()));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> roleRepository.findByName(USER_ROLE)
                        .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND))
        );

        assertEquals(ApplicationErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());
    }
}
