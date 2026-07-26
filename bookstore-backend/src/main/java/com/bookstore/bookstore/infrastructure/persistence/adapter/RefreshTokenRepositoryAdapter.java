package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IRefreshTokenRepository;
import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Optional;
import java.util.List;
import java.time.Instant;
import com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements IRefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final RefreshTokenPersistenceMapper refreshTokenPersistenceMapper;

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHash(tokenHash)
                .map(refreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public Optional<RefreshToken> findByTokenHashForUpdate(String tokenHash) {
        return refreshTokenJpaRepository.findByTokenHashForUpdate(tokenHash)
                .map(refreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public List<RefreshToken> findActiveByUserId(UUID userId) {
        return refreshTokenJpaRepository.findActiveByUserId(userId, Instant.now()).stream()
                .map(refreshTokenPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<RefreshToken> findByIdForUpdate(UUID id) {
        return refreshTokenJpaRepository.findByIdForUpdate(id)
                .map(refreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public Optional<RefreshToken> findById(UUID id) {
        return refreshTokenJpaRepository.findById(id)
                .map(refreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = refreshTokenJpaRepository.findById(refreshToken.getId())
                .orElseGet(RefreshTokenJpaEntity::new);
        
        UserJpaEntity user = userJpaRepository.getReferenceById(refreshToken.getUserId());
        refreshTokenPersistenceMapper.copyToEntity(refreshToken, entity, user);
        return refreshTokenPersistenceMapper.toDomain(refreshTokenJpaRepository.save(entity));
    }

    @Override
    public void revokeAllByUserId(UUID userId, Instant revokedAt, RefreshTokenRevokeReason reason) {
        refreshTokenJpaRepository.revokeAllByUserId(userId, revokedAt, reason);
    }

    @Override
    public void revokeFamily(UUID familyId, Instant revokedAt, RefreshTokenRevokeReason reason) {
        refreshTokenJpaRepository.revokeFamily(familyId, revokedAt, reason);
    }
}
