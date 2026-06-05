package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IPermissionService;
import com.bookstore.bookstore.application.port.in.IRoleService;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.enums.RoleName;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.domain.model.Role;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService implements IRoleService {

    private static final Set<PermissionCode> USER_PERMISSION_CODES = Set.of(
            PermissionCode.BOOK_VIEW,
            PermissionCode.CATEGORY_VIEW,
            PermissionCode.AUTHOR_VIEW,
            PermissionCode.PUBLISHER_VIEW,
            PermissionCode.ORDER_CREATE,
            PermissionCode.ORDER_VIEW_OWN,
            PermissionCode.ORDER_CANCEL_OWN,
            PermissionCode.REVIEW_CREATE
    );

    private static final Set<PermissionCode> STAFF_PERMISSION_CODES = Set.of(
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
            PermissionCode.REVIEW_MANAGE,
            PermissionCode.COUPON_MANAGE,
            PermissionCode.DASHBOARD_VIEW
    );

    private final Map<UUID, Role> rolesById = new LinkedHashMap<>();
    private final Map<RoleName, Role> rolesByName = new LinkedHashMap<>();
    private final IPermissionService permissionService;

    public RoleService(IPermissionService permissionService) {
        this.permissionService = permissionService;
        seedDefaultRoles();
    }

    @Override
    public List<Role> getAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public Role getByName(RoleName roleName) {
        if (roleName == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "roleName");
        }

        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    @Override
    public Set<PermissionCode> getPermissionCodes(RoleName roleName) {
        Role role = getByName(roleName);
        Set<PermissionCode> permissionCodes = new LinkedHashSet<>();
        for (Permission permission : role.getPermissions()) {
            permissionCodes.add(permission.getCode());
        }
        return Set.copyOf(permissionCodes);
    }

    @Override
    public boolean hasPermission(RoleName roleName, PermissionCode permissionCode) {
        if (permissionCode == null) {
            return false;
        }

        return getPermissionCodes(roleName).contains(permissionCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role create(Role role) {
        if (role == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "role");
        }

        if (rolesById.containsKey(role.getId())) {
            throw new ApplicationException(ApplicationErrorCode.ROLE_ALREADY_EXISTS);
        }

        if (rolesByName.containsKey(role.getName())) {
            throw new ApplicationException(ApplicationErrorCode.ROLE_NAME_ALREADY_EXISTS);
        }

        Role normalized = normalize(role);
        rolesById.put(normalized.getId(), normalized);
        rolesByName.put(normalized.getName(), normalized);
        return normalized;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role update(Role role) {
        if (role == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "role");
        }

        Role currentRole = rolesById.get(role.getId());
        if (currentRole == null) {
            throw new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND);
        }

        if (currentRole.getName() != role.getName() && rolesByName.containsKey(role.getName())) {
            throw new ApplicationException(ApplicationErrorCode.ROLE_NAME_ALREADY_EXISTS);
        }

        Role normalized = normalize(role);
        rolesById.put(normalized.getId(), normalized);
        rolesByName.remove(currentRole.getName());
        rolesByName.put(normalized.getName(), normalized);
        return normalized;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID roleId) {
        if (roleId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "roleId");
        }

        Role currentRole = rolesById.remove(roleId);
        if (currentRole == null) {
            throw new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND);
        }

        rolesByName.remove(currentRole.getName());
    }

    private void seedDefaultRoles() {
        Arrays.asList(RoleName.ADMIN, RoleName.USER, RoleName.STAFF)
                .stream()
                .map(this::buildDefaultRole)
                .forEach(role -> {
                    rolesById.put(role.getId(), role);
                    rolesByName.put(role.getName(), role);
                });
    }

    private Role buildDefaultRole(RoleName roleName) {
        switch (roleName) {
            case ADMIN:
                return buildRole(roleName, Set.of(PermissionCode.values()));
            case USER:
                return buildRole(roleName, USER_PERMISSION_CODES);
            case STAFF:
                return buildRole(roleName, STAFF_PERMISSION_CODES);
            default:
                throw new IllegalStateException("Unsupported role: " + roleName);
        }
    }

    private Role buildRole(RoleName roleName, Set<PermissionCode> permissionCodes) {
        Instant now = Instant.EPOCH;
        Set<Permission> permissions = new LinkedHashSet<>();
        for (PermissionCode permissionCode : permissionCodes) {
            Permission permission = permissionService.getByCode(permissionCode);
            if (permission == null) {
                throw new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND);
            }
            permissions.add(permission);
        }

        return new Role(
                UUID.nameUUIDFromBytes(("role:" + roleName.name()).getBytes()),
                roleName,
                roleName.name().toLowerCase().replace('_', ' ') + " role",
                permissions,
                now,
                now,
                null
        );
    }

    private Role normalize(Role role) {
        Set<Permission> permissions = new LinkedHashSet<>();
        for (Permission permission : role.getPermissions()) {
            Permission canonicalPermission = permissionService.getByCode(permission.getCode());
            if (canonicalPermission == null) {
                throw new ApplicationException(ApplicationErrorCode.PERMISSION_NOT_FOUND);
            }
            permissions.add(canonicalPermission);
        }

        return new Role(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissions,
                role.getCreatedAt() == null ? Instant.EPOCH : role.getCreatedAt(),
                role.getUpdatedAt(),
                role.getDeletedAt()
        );
    }
}
