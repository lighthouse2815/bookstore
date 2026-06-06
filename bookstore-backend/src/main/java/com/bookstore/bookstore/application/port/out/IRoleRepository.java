package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRoleRepository {

    List<Role> findAll();

    Optional<Role> findById(UUID roleId);

    Optional<Role> findByName(String roleName);

    boolean existsById(UUID roleId);

    boolean existsByName(String roleName);

    boolean existsByPermissionCode(PermissionCode permissionCode);

    Role save(Role role);

    void deleteById(UUID roleId);
}
