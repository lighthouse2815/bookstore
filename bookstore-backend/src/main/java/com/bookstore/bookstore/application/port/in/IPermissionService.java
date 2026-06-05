package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import java.util.List;
import java.util.UUID;

public interface IPermissionService {

    List<Permission> getAll();

    Permission getByCode(PermissionCode permissionCode);

    Permission create(Permission permission);

    Permission update(Permission permission);

    void delete(UUID permissionId);
}
