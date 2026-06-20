package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitalAssetJpaRepository extends JpaRepository<DigitalAssetJpaEntity, UUID> {

    Optional<DigitalAssetJpaEntity> findByIdAndDeletedAtIsNullAndBook_DeletedAtIsNull(UUID digitalAssetId);

    List<DigitalAssetJpaEntity> findAllByBook_IdAndDeletedAtIsNullAndBook_DeletedAtIsNullOrderByCreatedAtDesc(
            UUID bookId
    );

    List<DigitalAssetJpaEntity> findAllByBook_IdOrderByCreatedAtDesc(UUID bookId);

    List<DigitalAssetJpaEntity> findAllByBook_IdInAndDeletedAtIsNullAndBook_DeletedAtIsNullOrderByCreatedAtDesc(
            List<UUID> bookIds
    );

    List<DigitalAssetJpaEntity> findAllByIdInAndDeletedAtIsNullAndBook_DeletedAtIsNull(List<UUID> digitalAssetIds);

    Optional<DigitalAssetJpaEntity> findById(UUID digitalAssetId);
}
