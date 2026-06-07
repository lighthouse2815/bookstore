package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IPermissionRepository;
import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.infrastructure.persistence.entity.PermissionJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.PermissionPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.PermissionJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PermissionRepositoryAdapter implements IPermissionRepository {

    private final PermissionJpaRepository permissionJpaRepository;
    private final PermissionPersistenceMapper permissionPersistenceMapper;

    @Override
    public List<Permission> findAllActive() {
        return permissionJpaRepository.findAllActive().stream()
                .map(permissionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Permission> findAllIncludingDeleted() {
        return permissionJpaRepository.findAllIncludingDeleted().stream()
                .map(permissionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Permission> findByIdActive(UUID permissionId) {
        return permissionJpaRepository.findByIdActive(permissionId)
                .map(permissionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Permission> findByIdIncludingDeleted(UUID permissionId) {
        return permissionJpaRepository.findByIdIncludingDeleted(permissionId)
                .map(permissionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Permission> findByCodeActive(PermissionCode permissionCode) {
        return permissionJpaRepository.findByCodeActive(permissionCode)
                .map(permissionPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID permissionId) {
        return permissionJpaRepository.existsByIdIncludingDeleted(permissionId);
    }

    @Override
    public boolean existsByCodeIncludingDeleted(PermissionCode permissionCode) {
        return permissionJpaRepository.existsByCodeIncludingDeleted(permissionCode);
    }

    @Override
    public Permission save(Permission permission) {
        PermissionJpaEntity entity = permissionJpaRepository.findByIdIncludingDeleted(permission.getId())
                .orElseGet(PermissionJpaEntity::new);
        permissionPersistenceMapper.copyToEntity(permission, entity);
        return permissionPersistenceMapper.toDomain(permissionJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID permissionId) {
        permissionJpaRepository.deleteById(permissionId);
    }
}
