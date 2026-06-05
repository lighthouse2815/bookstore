package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPermissionRepository {

    List<Permission> findAll();

    Optional<Permission> findById(UUID permissionId);

    Optional<Permission> findByCode(PermissionCode permissionCode);

    boolean existsById(UUID permissionId);

    boolean existsByCode(PermissionCode permissionCode);

    Permission save(Permission permission);

    void deleteById(UUID permissionId);
}
