package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.UpdatePermissionCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IPermissionRepository;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private IPermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void getAll_returnsRepositoryPermissions() {
        Permission permission = permission(PermissionCode.BOOK_VIEW, "book view");
        when(permissionRepository.findAllActive()).thenReturn(List.of(permission));

        assertEquals(1, permissionService.getAll().size());
        assertEquals(PermissionCode.BOOK_VIEW, permissionService.getAll().get(0).getCode());
    }

    @Test
    void getById_returnsPermission() {
        Permission permission = permission(PermissionCode.BOOK_VIEW, "book view");
        when(permissionRepository.findByIdActive(permission.getId())).thenReturn(Optional.of(permission));

        Permission result = permissionService.getById(permission.getId());

        assertEquals(permission.getId(), result.getId());
        assertEquals(PermissionCode.BOOK_VIEW, result.getCode());
    }

    @Test
    void update_returnsUpdatedPermission() {
        Permission current = permission(PermissionCode.BOOK_VIEW, "book view");
        PermissionCode nextCode = PermissionCode.BOOK_CREATE;
        UpdatePermissionCommand command = new UpdatePermissionCommand(current.getId(), nextCode, null);

        when(permissionRepository.findByIdActive(current.getId())).thenReturn(Optional.of(current));
        when(permissionRepository.existsByCodeIncludingDeleted(nextCode)).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Permission saved = permissionService.update(command);

        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository).save(captor.capture());
        assertEquals(nextCode, captor.getValue().getCode());
        assertEquals(nextCode, saved.getCode());
    }

    @Test
    void update_rejectsDuplicateCode() {
        Permission current = permission(PermissionCode.BOOK_VIEW, "book view");
        PermissionCode nextCode = PermissionCode.BOOK_CREATE;
        UpdatePermissionCommand command = new UpdatePermissionCommand(current.getId(), nextCode, null);

        when(permissionRepository.findByIdActive(current.getId())).thenReturn(Optional.of(current));
        when(permissionRepository.existsByCodeIncludingDeleted(nextCode)).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> permissionService.update(command)
        );

        assertEquals(ApplicationErrorCode.PERMISSION_CODE_ALREADY_EXISTS, exception.getErrorCode());
    }

    private static Permission permission(PermissionCode code, String description) {
        Instant now = Instant.EPOCH;
        return new Permission(
                UUID.randomUUID(),
                code,
                description,
                now,
                now,
                null
        );
    }
}
