package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserDigitalAccessJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDigitalAccessJpaRepository extends JpaRepository<UserDigitalAccessJpaEntity, UUID> {

    Optional<UserDigitalAccessJpaEntity> findByIdAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNull(
            UUID userDigitalAccessId
    );

    List<UserDigitalAccessJpaEntity> findAllByUser_IdAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNullOrderByCreatedAtDesc(
            UUID userId
    );

    List<UserDigitalAccessJpaEntity> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<UserDigitalAccessJpaEntity> findAllByUser_IdAndDigitalAsset_IdAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNull(
            UUID userId,
            UUID digitalAssetId
    );

    List<UserDigitalAccessJpaEntity> findAllBySourceOrderIdAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNullOrderByCreatedAtDesc(
            UUID sourceOrderId
    );

    boolean existsByUser_IdAndDigitalAsset_IdAndStatusAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNull(
            UUID userId,
            UUID digitalAssetId,
            DigitalAccessStatus status
    );

    Optional<UserDigitalAccessJpaEntity> findById(UUID userDigitalAccessId);
}
