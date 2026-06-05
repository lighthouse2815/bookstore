package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IPermissionService;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionService implements IPermissionService {

    private final Map<UUID, Permission> permissionsById = new LinkedHashMap<>();
    private final Map<PermissionCode, Permission> permissionsByCode = new LinkedHashMap<>();

    public PermissionService() {
        seedDefaultPermissions();
    }

    @Override
    public List<Permission> getAll() {
        return new ArrayList<>(permissionsById.values());
    }

    @Override
    public Permission getByCode(PermissionCode permissionCode) {
        if (permissionCode == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permissionCode");
        }

        Permission permission = permissionsByCode.get(permissionCode);
        if (permission == null) {
            throw new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND);
        }
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission create(Permission permission) {
        if (permission == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permission");
        }

        if (permissionsById.containsKey(permission.getId())) {
            throw new ApplicationException(ApplicationErrorCode.PERMISSION_ALREADY_EXISTS);
        }

        if (permissionsByCode.containsKey(permission.getCode())) {
            throw new ApplicationException(ApplicationErrorCode.PERMISSION_CODE_ALREADY_EXISTS);
        }

        permissionsById.put(permission.getId(), permission);
        permissionsByCode.put(permission.getCode(), permission);
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission update(Permission permission) {
        if (permission == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permission");
        }

        Permission currentPermission = permissionsById.get(permission.getId());
        if (currentPermission == null) {
            throw new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND);
        }

        if (currentPermission.getCode() != permission.getCode()
                && permissionsByCode.containsKey(permission.getCode())) {
            throw new ApplicationException(ApplicationErrorCode.PERMISSION_CODE_ALREADY_EXISTS);
        }

        permissionsById.put(permission.getId(), permission);
        permissionsByCode.remove(currentPermission.getCode());
        permissionsByCode.put(permission.getCode(), permission);
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID permissionId) {
        if (permissionId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "permissionId");
        }

        Permission currentPermission = permissionsById.remove(permissionId);
        if (currentPermission == null) {
            throw new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND);
        }

        permissionsByCode.remove(currentPermission.getCode());
    }

    private void seedDefaultPermissions() {
        Arrays.stream(PermissionCode.values())
                .map(PermissionService::buildDefaultPermission)
                .forEach(permission -> {
                    permissionsById.put(permission.getId(), permission);
                    permissionsByCode.put(permission.getCode(), permission);
                });
    }

    private static Permission buildDefaultPermission(PermissionCode permissionCode) {
        Instant now = Instant.EPOCH;
        return new Permission(
                UUID.nameUUIDFromBytes(("permission:" + permissionCode.name()).getBytes()),
                permissionCode,
                permissionCode.name().toLowerCase().replace('_', ' '),
                now,
                now,
                null
        );
    }
}
