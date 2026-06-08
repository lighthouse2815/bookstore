package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new RefreshToken(
                entity.getId(),
                entity.getUserId(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.isRevoked(),
                entity.getCreatedAt()
        );
    }

    public void copyToEntity(RefreshToken refreshToken, RefreshTokenJpaEntity entity) {
        entity.setId(refreshToken.getId());
        entity.setUserId(refreshToken.getUserId());
        entity.setToken(refreshToken.getToken());
        entity.setExpiresAt(refreshToken.getExpiresAt());
        entity.setRevoked(refreshToken.isRevoked());
        entity.setCreatedAt(refreshToken.getCreatedAt());
    }
}
