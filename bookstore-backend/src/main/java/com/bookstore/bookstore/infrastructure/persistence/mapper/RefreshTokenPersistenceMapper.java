package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenPersistenceMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new RefreshToken(
                entity.getId(),
                entity.getUser().getId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.isRevoked(),
                entity.getCreatedAt()
        );
    }

    public void copyToEntity(RefreshToken refreshToken, RefreshTokenJpaEntity entity, UserJpaEntity user) {
        entity.setId(refreshToken.getId());
        entity.setUser(user);
        entity.setTokenHash(refreshToken.getTokenHash());
        entity.setExpiresAt(refreshToken.getExpiresAt());
        entity.setRevoked(refreshToken.isRevoked());
        entity.setCreatedAt(refreshToken.getCreatedAt());
    }
}
