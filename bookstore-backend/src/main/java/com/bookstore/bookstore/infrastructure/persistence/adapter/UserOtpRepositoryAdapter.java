package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.domain.enums.OtpPurpose;
import com.bookstore.bookstore.application.port.out.IUserOtpRepository;
import com.bookstore.bookstore.domain.model.UserOtp;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserOtpJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.UserOtpPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
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
    private final UserJpaRepository userJpaRepository;
    private final UserOtpPersistenceMapper userOtpPersistenceMapper;

    @Override
    public Optional<UserOtp> findLatestByUserIdAndPurpose(UUID userId, OtpPurpose purpose) {
        return userOtpJpaRepository.findFirstByUserIdAndPurposeOrderByCreatedAtDesc(userId, purpose)
                .map(userOtpPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserOtp> findLatestPendingByUserIdAndPurpose(UUID userId, OtpPurpose purpose) {
        return userOtpJpaRepository
                .findFirstByUserIdAndPurposeAndVerifiedAtIsNullAndInvalidatedAtIsNullOrderByCreatedAtDesc(
                        userId,
                        purpose
                )
                .map(userOtpPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserOtp> findOldestByUserIdAndPurposeCreatedAfter(UUID userId, OtpPurpose purpose, Instant createdAfter) {
        return userOtpJpaRepository
                .findFirstByUserIdAndPurposeAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
                        userId,
                        purpose,
                        createdAfter
                )
                .map(userOtpPersistenceMapper::toDomain);
    }

    @Override
    public long countByUserIdAndPurposeCreatedAfter(UUID userId, OtpPurpose purpose, Instant createdAfter) {
        return userOtpJpaRepository.countByUserIdAndPurposeAndCreatedAtGreaterThanEqual(
                userId,
                purpose,
                createdAfter
        );
    }

    @Override
    public void invalidatePendingByUserIdAndPurpose(UUID userId, OtpPurpose purpose, Instant invalidatedAt) {
        userOtpJpaRepository.invalidatePendingByUserIdAndPurpose(userId, purpose, invalidatedAt);
    }

    @Override
    public void invalidateActiveByUserIdAndPurpose(UUID userId, OtpPurpose purpose, Instant invalidatedAt) {
        userOtpJpaRepository.invalidateActiveByUserIdAndPurpose(userId, purpose, invalidatedAt);
    }

    @Override
    public UserOtp save(UserOtp userOtp) {
        UserOtpJpaEntity entity = userOtpJpaRepository.findById(userOtp.getId())
                .orElseGet(UserOtpJpaEntity::new);
        
        UserJpaEntity user = userJpaRepository.getReferenceById(userOtp.getUserId());
        userOtpPersistenceMapper.copyToEntity(userOtp, entity, user);
        return userOtpPersistenceMapper.toDomain(userOtpJpaRepository.save(entity));
    }
}
