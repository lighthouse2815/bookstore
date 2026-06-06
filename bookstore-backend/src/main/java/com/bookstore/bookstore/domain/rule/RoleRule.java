package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Permission;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleRule {

    private RoleRule() {
    }

    public static void requireCanUpdate(
            Instant deletedAt,
            String currentName,
            String currentDescription,
            Set<Permission> currentPermissions,
            String newName,
            String newDescription,
            Set<Permission> newPermissions
    ) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.ROLE_ALREADY_DELETED);
        }

        boolean unchangedName = Objects.equals(currentName, newName);
        boolean unchangedDescription = Objects.equals(currentDescription, newDescription);
        boolean unchangedPermissions = toPermissionCodes(currentPermissions).equals(toPermissionCodes(newPermissions));

        if (unchangedName && unchangedDescription && unchangedPermissions) {
            throw new DomainException(DomainErrorCode.ROLE_DATA_NOT_CHANGED);
        }
    }

    public static void requireCanSoftDelete(Instant deletedAt) {
        if (deletedAt != null) {
            throw new DomainException(DomainErrorCode.ROLE_ALREADY_DELETED);
        }
    }

    private static Set<PermissionCode> toPermissionCodes(Set<Permission> permissions) {
        if (permissions == null) {
            return Collections.emptySet();
        }

        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }
}
