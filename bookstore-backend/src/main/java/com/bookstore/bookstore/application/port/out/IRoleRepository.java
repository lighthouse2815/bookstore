package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRoleRepository {

    List<Role> findAllActive();

    List<Role> findAllIncludingDeleted();

    Optional<Role> findByIdActive(UUID roleId);

    Optional<Role> findByIdIncludingDeleted(UUID roleId);

    Optional<Role> findByNameActive(String roleName);

    boolean existsByIdIncludingDeleted(UUID roleId);

    boolean existsByNameIncludingDeleted(String roleName);

    boolean existsByPermissionCodeIncludingDeleted(PermissionCode permissionCode);

    Role save(Role role);

    void deleteById(UUID roleId);
}
