package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ReadingProgressJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingProgressJpaRepository extends JpaRepository<ReadingProgressJpaEntity, UUID> {

    List<ReadingProgressJpaEntity> findAllByUser_IdOrderByUpdatedAtDesc(UUID userId);

    Optional<ReadingProgressJpaEntity> findByUser_IdAndDigitalAsset_Id(UUID userId, UUID digitalAssetId);

    Optional<ReadingProgressJpaEntity> findById(UUID readingProgressId);
}
