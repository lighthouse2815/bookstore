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
                entity.getFamilyId(),
                entity.getParentTokenId(),
                entity.getReplacedByTokenId(),
                entity.getDeviceId(),
                entity.getDeviceName(),
                entity.getUserAgent(),
                entity.getIpAddress(),
                entity.getIssuedAt(),
                entity.getLastUsedAt(),
                entity.getRevokedAt(),
                entity.getRevokeReason(),
                entity.getExpiresAt(),
                entity.isRevoked(),
                entity.getCreatedAt()
        );
    }

    public void copyToEntity(RefreshToken refreshToken, RefreshTokenJpaEntity entity, UserJpaEntity user) {
        entity.setId(refreshToken.getId());
        entity.setUser(user);
        entity.setTokenHash(refreshToken.getTokenHash());
        entity.setFamilyId(refreshToken.getFamilyId());
        entity.setParentTokenId(refreshToken.getParentTokenId());
        entity.setReplacedByTokenId(refreshToken.getReplacedByTokenId());
        entity.setDeviceId(refreshToken.getDeviceId());
        entity.setDeviceName(refreshToken.getDeviceName());
        entity.setUserAgent(refreshToken.getUserAgent());
        entity.setIpAddress(refreshToken.getIpAddress());
        entity.setIssuedAt(refreshToken.getIssuedAt());
        entity.setLastUsedAt(refreshToken.getLastUsedAt());
        entity.setRevokedAt(refreshToken.getRevokedAt());
        entity.setRevokeReason(refreshToken.getRevokeReason());
        entity.setExpiresAt(refreshToken.getExpiresAt());
        entity.setRevoked(refreshToken.isRevoked());
        entity.setCreatedAt(refreshToken.getCreatedAt());
    }
}
