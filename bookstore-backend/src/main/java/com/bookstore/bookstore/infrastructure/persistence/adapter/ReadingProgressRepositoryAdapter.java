package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IReadingProgressRepository;
import com.bookstore.bookstore.domain.model.ReadingProgress;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReadingProgressJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ReadingProgressPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.DigitalAssetJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.ReadingProgressJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReadingProgressRepositoryAdapter implements IReadingProgressRepository {

    private final ReadingProgressJpaRepository readingProgressJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final DigitalAssetJpaRepository digitalAssetJpaRepository;
    private final ReadingProgressPersistenceMapper readingProgressPersistenceMapper;

    @Override
    public Optional<ReadingProgress> findById(UUID readingProgressId) {
        return readingProgressJpaRepository.findById(readingProgressId)
                .map(readingProgressPersistenceMapper::toDomain);
    }

    @Override
    public List<ReadingProgress> findAllByUserId(UUID userId) {
        return readingProgressJpaRepository.findAllByUser_IdOrderByUpdatedAtDesc(userId).stream()
                .map(readingProgressPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ReadingProgress> findByUserIdAndDigitalAssetId(UUID userId, UUID digitalAssetId) {
        return readingProgressJpaRepository.findByUser_IdAndDigitalAsset_Id(userId, digitalAssetId)
                .map(readingProgressPersistenceMapper::toDomain);
    }

    @Override
    public ReadingProgress save(ReadingProgress readingProgress) {
        ReadingProgressJpaEntity entity = readingProgressJpaRepository.findById(readingProgress.getId())
                .orElseGet(ReadingProgressJpaEntity::new);
        UserJpaEntity user = userJpaRepository.getReferenceById(readingProgress.getUserId());
        DigitalAssetJpaEntity digitalAsset = digitalAssetJpaRepository.getReferenceById(
                readingProgress.getDigitalAssetId()
        );

        readingProgressPersistenceMapper.copyToEntity(readingProgress, entity, user, digitalAsset);
        return readingProgressPersistenceMapper.toDomain(readingProgressJpaRepository.save(entity));
    }
}
