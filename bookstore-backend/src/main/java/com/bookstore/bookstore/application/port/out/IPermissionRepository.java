package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPermissionRepository {

    List<Permission> findAllActive();

    List<Permission> findAllIncludingDeleted();

    Optional<Permission> findByIdActive(UUID permissionId);

    Optional<Permission> findByIdIncludingDeleted(UUID permissionId);

    Optional<Permission> findByCodeActive(PermissionCode permissionCode);

    boolean existsByIdIncludingDeleted(UUID permissionId);

    boolean existsByCodeIncludingDeleted(PermissionCode permissionCode);

    Permission save(Permission permission);

    void deleteById(UUID permissionId);
}
