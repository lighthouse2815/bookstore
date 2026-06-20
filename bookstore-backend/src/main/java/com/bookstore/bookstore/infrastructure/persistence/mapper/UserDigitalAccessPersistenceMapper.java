package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.UserDigitalAccess;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserDigitalAccessJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserDigitalAccessPersistenceMapper {

    public UserDigitalAccess toDomain(UserDigitalAccessJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new UserDigitalAccess(
                entity.getId(),
                entity.getUser().getId(),
                entity.getDigitalAsset().getId(),
                entity.getAccessType(),
                entity.getStatus(),
                entity.getSourceOrderId(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            UserDigitalAccess userDigitalAccess,
            UserDigitalAccessJpaEntity entity,
            UserJpaEntity user,
            DigitalAssetJpaEntity digitalAsset
    ) {
        entity.setId(userDigitalAccess.getId());
        entity.setUser(user);
        entity.setDigitalAsset(digitalAsset);
        entity.setAccessType(userDigitalAccess.getAccessType());
        entity.setStatus(userDigitalAccess.getStatus());
        entity.setSourceOrderId(userDigitalAccess.getSourceOrderId());
        entity.setExpiresAt(userDigitalAccess.getExpiresAt());
        entity.setCreatedAt(userDigitalAccess.getCreatedAt());
        entity.setUpdatedAt(userDigitalAccess.getUpdatedAt());
        entity.setDeletedAt(userDigitalAccess.getDeletedAt());
    }
}
