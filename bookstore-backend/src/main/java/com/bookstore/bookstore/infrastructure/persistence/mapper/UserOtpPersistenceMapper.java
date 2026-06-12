package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.UserOtp;
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
                entity.getUserId(),
                entity.getOtpHash(),
                entity.getExpiresAt(),
                entity.getVerifiedAt(),
                entity.getInvalidatedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void copyToEntity(UserOtp userOtp, UserOtpJpaEntity entity) {
        entity.setId(userOtp.getId());
        entity.setUserId(userOtp.getUserId());
        entity.setOtpHash(userOtp.getOtpHash());
        entity.setExpiresAt(userOtp.getExpiresAt());
        entity.setVerifiedAt(userOtp.getVerifiedAt());
        entity.setInvalidatedAt(userOtp.getInvalidatedAt());
        entity.setCreatedAt(userOtp.getCreatedAt());
        entity.setUpdatedAt(userOtp.getUpdatedAt());
    }
}
