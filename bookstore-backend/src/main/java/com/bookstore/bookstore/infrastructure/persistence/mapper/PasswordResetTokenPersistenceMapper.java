package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.PasswordResetToken;
import com.bookstore.bookstore.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenPersistenceMapper {

    public PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PasswordResetToken(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }

    public void copyToEntity(PasswordResetToken passwordResetToken, PasswordResetTokenJpaEntity entity) {
        entity.setId(passwordResetToken.getId());
        entity.setUserId(passwordResetToken.getUserId());
        entity.setTokenHash(passwordResetToken.getTokenHash());
        entity.setExpiresAt(passwordResetToken.getExpiresAt());
        entity.setUsedAt(passwordResetToken.getUsedAt());
        entity.setCreatedAt(passwordResetToken.getCreatedAt());
    }
}
