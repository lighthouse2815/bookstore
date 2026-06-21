package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IPasswordResetTokenRepository;
import com.bookstore.bookstore.domain.model.PasswordResetToken;
import com.bookstore.bookstore.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.PasswordResetTokenPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements IPasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordResetTokenPersistenceMapper passwordResetTokenPersistenceMapper;

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return passwordResetTokenJpaRepository.findByTokenHash(tokenHash)
                .map(passwordResetTokenPersistenceMapper::toDomain);
    }

    @Override
    public void markUnusedByUserIdAsUsed(UUID userId, Instant usedAt) {
        passwordResetTokenJpaRepository.markUnusedByUserIdAsUsed(userId, usedAt);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        PasswordResetTokenJpaEntity entity = passwordResetTokenJpaRepository.findById(passwordResetToken.getId())
                .orElseGet(PasswordResetTokenJpaEntity::new);
        
        UserJpaEntity user = userJpaRepository.getReferenceById(passwordResetToken.getUserId());
        passwordResetTokenPersistenceMapper.copyToEntity(passwordResetToken, entity, user);
        return passwordResetTokenPersistenceMapper.toDomain(passwordResetTokenJpaRepository.save(entity));
    }
}
