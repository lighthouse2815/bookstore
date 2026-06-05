package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.enums.RoleName;
import com.bookstore.bookstore.domain.model.Role;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IRoleService {

    List<Role> getAll();

    Role getByName(RoleName roleName);

    Set<PermissionCode> getPermissionCodes(RoleName roleName);

    boolean hasPermission(RoleName roleName, PermissionCode permissionCode);

    Role create(Role role);

    Role update(Role role);

    void delete(UUID roleId);
}
