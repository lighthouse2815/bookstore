package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.ReadingProgress;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReadingProgressJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReadingProgressPersistenceMapper {

    public ReadingProgress toDomain(ReadingProgressJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ReadingProgress(
                entity.getId(),
                entity.getUser().getId(),
                entity.getDigitalAsset().getId(),
                entity.getCurrentPage(),
                entity.getProgressPercent(),
                entity.getPositionData(),
                entity.getLastReadAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void copyToEntity(
            ReadingProgress readingProgress,
            ReadingProgressJpaEntity entity,
            UserJpaEntity user,
            DigitalAssetJpaEntity digitalAsset
    ) {
        entity.setId(readingProgress.getId());
        entity.setUser(user);
        entity.setDigitalAsset(digitalAsset);
        entity.setCurrentPage(readingProgress.getCurrentPage());
        entity.setProgressPercent(readingProgress.getProgressPercent());
        entity.setPositionData(readingProgress.getPositionData());
        entity.setLastReadAt(readingProgress.getLastReadAt());
        entity.setCreatedAt(readingProgress.getCreatedAt());
        entity.setUpdatedAt(readingProgress.getUpdatedAt());
    }
}
