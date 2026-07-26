package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ProfileJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileJpaRepository extends JpaRepository<ProfileJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"user", "avatarFileAsset"})
    List<ProfileJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = {"user", "avatarFileAsset"})
    List<ProfileJpaEntity> findAll();

    @EntityGraph(attributePaths = {"user", "avatarFileAsset"})
    Optional<ProfileJpaEntity> findByIdAndDeletedAtIsNull(UUID profileId);

    @EntityGraph(attributePaths = {"user", "avatarFileAsset"})
    Optional<ProfileJpaEntity> findById(UUID profileId);

    @EntityGraph(attributePaths = {"user", "avatarFileAsset"})
    Optional<ProfileJpaEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsById(UUID profileId);

    @EntityGraph(attributePaths = {"user", "avatarFileAsset"})
    Optional<ProfileJpaEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
