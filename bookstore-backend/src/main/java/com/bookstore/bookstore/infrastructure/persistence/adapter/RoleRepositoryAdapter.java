package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.infrastructure.persistence.entity.PermissionJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.RoleJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.RolePersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.PermissionJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.RoleJpaRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements IRoleRepository {

    private final RoleJpaRepository roleJpaRepository;
    private final PermissionJpaRepository permissionJpaRepository;
    private final RolePersistenceMapper rolePersistenceMapper;

    @Override
    public List<Role> findAllActive() {
        return roleJpaRepository.findAllActive().stream()
                .map(rolePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Role> findAllIncludingDeleted() {
        return roleJpaRepository.findAllIncludingDeleted().stream()
                .map(rolePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Role> findByIdActive(UUID roleId) {
        return roleJpaRepository.findByIdActive(roleId)
                .map(rolePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Role> findByIdIncludingDeleted(UUID roleId) {
        return roleJpaRepository.findByIdIncludingDeleted(roleId)
                .map(rolePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Role> findByNameActive(String roleName) {
        return roleJpaRepository.findByNameActive(roleName)
                .map(rolePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID roleId) {
        return roleJpaRepository.existsByIdIncludingDeleted(roleId);
    }

    @Override
    public boolean existsByNameIncludingDeleted(String roleName) {
        return roleJpaRepository.existsByNameIncludingDeleted(roleName);
    }

    @Override
    public boolean existsByPermissionCodeIncludingDeleted(PermissionCode permissionCode) {
        return roleJpaRepository.existsByPermissionsCodeIncludingDeleted(permissionCode);
    }

    @Override
    public Role save(Role role) {
        RoleJpaEntity entity = roleJpaRepository.findByIdIncludingDeleted(role.getId())
                .orElseGet(RoleJpaEntity::new);
        rolePersistenceMapper.copyToEntity(role, entity, resolvePermissions(role.getPermissions()));
        return rolePersistenceMapper.toDomain(roleJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID roleId) {
        roleJpaRepository.deleteById(roleId);
    }

    private Set<PermissionJpaEntity> resolvePermissions(Set<Permission> permissions) {
        Set<PermissionJpaEntity> resolved = new LinkedHashSet<>();
        for (Permission permission : permissions) {
            PermissionJpaEntity entity = permissionJpaRepository.findByIdIncludingDeleted(permission.getId())
                    .orElseThrow(() -> new IllegalStateException("Permission not found: " + permission.getId()));
            resolved.add(entity);
        }
        return resolved;
    }
}
