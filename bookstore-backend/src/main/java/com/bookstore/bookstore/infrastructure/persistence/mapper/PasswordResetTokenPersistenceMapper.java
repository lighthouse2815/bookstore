package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.PasswordResetToken;
import com.bookstore.bookstore.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenPersistenceMapper {

    public PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PasswordResetToken(
                entity.getId(),
                entity.getUser().getId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }

    public void copyToEntity(PasswordResetToken passwordResetToken, PasswordResetTokenJpaEntity entity, UserJpaEntity user) {
        entity.setId(passwordResetToken.getId());
        entity.setUser(user);
        entity.setTokenHash(passwordResetToken.getTokenHash());
        entity.setExpiresAt(passwordResetToken.getExpiresAt());
        entity.setUsedAt(passwordResetToken.getUsedAt());
        entity.setCreatedAt(passwordResetToken.getCreatedAt());
    }
}
