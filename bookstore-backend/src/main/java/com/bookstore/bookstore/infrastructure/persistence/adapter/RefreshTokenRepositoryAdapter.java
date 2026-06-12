package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IRefreshTokenRepository;
import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements IRefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final RefreshTokenPersistenceMapper refreshTokenPersistenceMapper;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token)
                .map(refreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = refreshTokenJpaRepository.findById(refreshToken.getId())
                .orElseGet(RefreshTokenJpaEntity::new);
        refreshTokenPersistenceMapper.copyToEntity(refreshToken, entity);
        return refreshTokenPersistenceMapper.toDomain(refreshTokenJpaRepository.save(entity));
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        refreshTokenJpaRepository.revokeAllByUserId(userId);
    }
}
