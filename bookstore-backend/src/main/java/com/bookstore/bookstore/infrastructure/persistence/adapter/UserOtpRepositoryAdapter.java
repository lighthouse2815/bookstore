package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IUserOtpRepository;
import com.bookstore.bookstore.domain.model.UserOtp;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserOtpJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.UserOtpPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserOtpJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserOtpRepositoryAdapter implements IUserOtpRepository {

    private final UserOtpJpaRepository userOtpJpaRepository;
    private final UserOtpPersistenceMapper userOtpPersistenceMapper;

    @Override
    public Optional<UserOtp> findLatestPendingByUserId(UUID userId) {
        return userOtpJpaRepository.findFirstByUserIdAndVerifiedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(
                        userId
                )
                .map(userOtpPersistenceMapper::toDomain);
    }

    @Override
    public void invalidatePendingByUserId(UUID userId, Instant invalidatedAt) {
        userOtpJpaRepository.invalidatePendingByUserId(userId, invalidatedAt);
    }

    @Override
    public UserOtp save(UserOtp userOtp) {
        UserOtpJpaEntity entity = userOtpJpaRepository.findById(userOtp.getId())
                .orElseGet(UserOtpJpaEntity::new);
        userOtpPersistenceMapper.copyToEntity(userOtp, entity);
        return userOtpPersistenceMapper.toDomain(userOtpJpaRepository.save(entity));
    }
}
