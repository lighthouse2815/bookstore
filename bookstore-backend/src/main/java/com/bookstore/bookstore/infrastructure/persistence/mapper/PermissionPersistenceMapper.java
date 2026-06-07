package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.infrastructure.persistence.entity.PermissionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PermissionPersistenceMapper {

    public Permission toDomain(PermissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Permission(
                entity.getId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public PermissionJpaEntity toEntity(Permission permission) {
        PermissionJpaEntity entity = new PermissionJpaEntity();
        copyToEntity(permission, entity);
        return entity;
    }

    public void copyToEntity(Permission permission, PermissionJpaEntity entity) {
        entity.setId(permission.getId());
        entity.setCode(permission.getCode());
        entity.setDescription(permission.getDescription());
        entity.setCreatedAt(permission.getCreatedAt());
        entity.setUpdatedAt(permission.getUpdatedAt());
        entity.setDeletedAt(permission.getDeletedAt());
    }
}
