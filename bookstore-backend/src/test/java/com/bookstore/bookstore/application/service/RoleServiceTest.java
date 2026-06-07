package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CreateRoleCommand;
import com.bookstore.bookstore.application.command.DeleteRoleCommand;
import com.bookstore.bookstore.application.command.UpdateRoleCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IPermissionRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.domain.model.Role;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    private static final String USER_ROLE = "USER";

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IPermissionRepository permissionRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void getAll_returnsRepositoryRoles() {
        Role role = role(USER_ROLE, "user role", Set.of(permission(PermissionCode.BOOK_VIEW)));
        when(roleRepository.findAllActive()).thenReturn(List.of(role));

        assertEquals(1, roleService.getAll().size());
        assertEquals(USER_ROLE, roleService.getAll().get(0).getName());
    }

    @Test
    void create_rejectsDuplicateName() {
        when(roleRepository.existsByNameIncludingDeleted(USER_ROLE)).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> roleService.create(new CreateRoleCommand(
                        USER_ROLE,
                        "user role",
                        Set.of(PermissionCode.BOOK_VIEW)
                ))
        );

        assertEquals(ApplicationErrorCode.ROLE_NAME_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void create_resolvesPermissionsAndSavesRole() {
        Permission viewPermission = permission(PermissionCode.BOOK_VIEW);
        Permission createPermission = permission(PermissionCode.BOOK_CREATE);

        when(roleRepository.existsByNameIncludingDeleted("STAFF")).thenReturn(false);
        when(permissionRepository.findByCodeActive(PermissionCode.BOOK_VIEW)).thenReturn(Optional.of(viewPermission));
        when(permissionRepository.findByCodeActive(PermissionCode.BOOK_CREATE)).thenReturn(Optional.of(createPermission));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role saved = roleService.create(new CreateRoleCommand(
                "STAFF",
                "staff role",
                new LinkedHashSet<>(Set.of(PermissionCode.BOOK_VIEW, PermissionCode.BOOK_CREATE))
        ));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertEquals("STAFF", captor.getValue().getName());
        assertEquals("staff role", captor.getValue().getDescription());
        assertEquals(2, captor.getValue().getPermissions().size());
        assertEquals(saved.getName(), captor.getValue().getName());
    }

    @Test
    void update_returnsUpdatedRole() {
        Role current = role(USER_ROLE, "user role", Set.of(permission(PermissionCode.BOOK_VIEW)));
        when(roleRepository.findByIdActive(current.getId())).thenReturn(Optional.of(current));
        when(permissionRepository.findByCodeActive(PermissionCode.BOOK_VIEW))
                .thenReturn(Optional.of(permission(PermissionCode.BOOK_VIEW)));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role saved = roleService.update(new UpdateRoleCommand(
                current.getId(),
                current.getName(),
                "updated role",
                Set.of(PermissionCode.BOOK_VIEW)
        ));

        assertEquals("updated role", saved.getDescription());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void delete_softDeletesRole() {
        Role current = role(USER_ROLE, "user role", Set.of(permission(PermissionCode.BOOK_VIEW)));
        when(roleRepository.findByIdActive(current.getId())).thenReturn(Optional.of(current));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        roleService.delete(new DeleteRoleCommand(current.getId()));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertFalse(captor.getValue().getDeletedAt() == null);
    }

    private static Role role(String name, String description, Set<Permission> permissions) {
        Instant now = Instant.EPOCH;
        return new Role(
                UUID.randomUUID(),
                name,
                description,
                permissions,
                now,
                now,
                null
        );
    }

    private static Permission permission(PermissionCode code) {
        Instant now = Instant.EPOCH;
        return new Permission(
                UUID.randomUUID(),
                code,
                code.name().toLowerCase(),
                now,
                now,
                null
        );
    }
}
