package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.infrastructure.persistence.entity.PermissionJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.RoleJpaEntity;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RolePersistenceMapper {

    private final PermissionPersistenceMapper permissionPersistenceMapper;

    public Role toDomain(RoleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Role(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPermissions().stream()
                        .map(permissionPersistenceMapper::toDomain)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public RoleJpaEntity toEntity(Role role, Set<PermissionJpaEntity> permissions) {
        RoleJpaEntity entity = new RoleJpaEntity();
        copyToEntity(role, entity, permissions);
        return entity;
    }

    public void copyToEntity(Role role, RoleJpaEntity entity, Set<PermissionJpaEntity> permissions) {
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setPermissions(new LinkedHashSet<>(permissions));
        entity.setCreatedAt(role.getCreatedAt());
        entity.setUpdatedAt(role.getUpdatedAt());
        entity.setDeletedAt(role.getDeletedAt());
    }
}
