package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.UserOtp;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserOtpJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserOtpPersistenceMapper {

    public UserOtp toDomain(UserOtpJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new UserOtp(
                entity.getId(),
                entity.getUser().getId(),
                entity.getPurpose(),
                entity.getOtpHash(),
                entity.getAttemptCount(),
                entity.getMaxAttempts(),
                entity.getLastAttemptAt(),
                entity.getExpiresAt(),
                entity.getVerifiedAt(),
                entity.getInvalidatedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void copyToEntity(UserOtp userOtp, UserOtpJpaEntity entity, UserJpaEntity user) {
        entity.setId(userOtp.getId());
        entity.setUser(user);
        entity.setPurpose(userOtp.getPurpose());
        entity.setOtpHash(userOtp.getOtpHash());
        entity.setAttemptCount(userOtp.getAttemptCount());
        entity.setMaxAttempts(userOtp.getMaxAttempts());
        entity.setLastAttemptAt(userOtp.getLastAttemptAt());
        entity.setExpiresAt(userOtp.getExpiresAt());
        entity.setVerifiedAt(userOtp.getVerifiedAt());
        entity.setInvalidatedAt(userOtp.getInvalidatedAt());
        entity.setCreatedAt(userOtp.getCreatedAt());
        entity.setUpdatedAt(userOtp.getUpdatedAt());
    }
}
