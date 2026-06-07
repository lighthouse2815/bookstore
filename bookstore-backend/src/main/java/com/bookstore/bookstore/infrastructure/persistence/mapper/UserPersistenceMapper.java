package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.infrastructure.persistence.entity.RoleJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceMapper {

    private final RolePersistenceMapper rolePersistenceMapper;

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getStatus(),
                entity.isLocked(),
                entity.getRoles().stream()
                        .map(rolePersistenceMapper::toDomain)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public UserJpaEntity toEntity(User user, Set<RoleJpaEntity> roles) {
        UserJpaEntity entity = new UserJpaEntity();
        copyToEntity(user, entity, roles);
        return entity;
    }

    public void copyToEntity(User user, UserJpaEntity entity, Set<RoleJpaEntity> roles) {
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setEmail(user.getEmail());
        entity.setStatus(user.getStatus());
        entity.setLocked(user.isLocked());
        entity.setRoles(new LinkedHashSet<>(roles));
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setDeletedAt(user.getDeletedAt());
    }
}
