package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.ReadingProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReadingProgressRepository {

    Optional<ReadingProgress> findById(UUID readingProgressId);

    List<ReadingProgress> findAllByUserId(UUID userId);

    Optional<ReadingProgress> findByUserIdAndDigitalAssetId(UUID userId, UUID digitalAssetId);

    ReadingProgress save(ReadingProgress readingProgress);
}
