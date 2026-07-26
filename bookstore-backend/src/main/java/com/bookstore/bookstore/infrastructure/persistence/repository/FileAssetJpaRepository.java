package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileAssetJpaRepository extends JpaRepository<FileAssetJpaEntity, UUID> {

    Optional<FileAssetJpaEntity> findByIdAndStatusAndDeletedAtIsNull(UUID id, FileStatus status);

    List<FileAssetJpaEntity> findAllByIdInAndStatusAndDeletedAtIsNull(Collection<UUID> ids, FileStatus status);

    @Query("""
            select coalesce(sum(f.sizeBytes), 0)
            from FileAssetJpaEntity f
            where f.deletedAt is null
              and f.status in :statuses
            """)
    long calculateReservedStorageBytes(@Param("statuses") Collection<FileStatus> statuses);

    long countByCreatedAtGreaterThanEqual(Instant createdAt);

    @Query("""
            select count(bi)
            from BookImageJpaEntity bi
            where bi.fileAsset.id = :fileAssetId
            """)
    long countBookImageUsages(@Param("fileAssetId") UUID fileAssetId);

    @Query("""
            select count(p)
            from ProfileJpaEntity p
            where p.avatarFileAsset.id = :fileAssetId
            """)
    long countProfileAvatarUsages(@Param("fileAssetId") UUID fileAssetId);

    @Query("""
            select count(a)
            from AuthorJpaEntity a
            where a.avatarFileAsset.id = :fileAssetId
            """)
    long countAuthorAvatarUsages(@Param("fileAssetId") UUID fileAssetId);

    @Query("""
            select count(c)
            from CategoryJpaEntity c
            where c.imageFileAsset.id = :fileAssetId
            """)
    long countCategoryImageUsages(@Param("fileAssetId") UUID fileAssetId);

    @Query("""
            select count(p)
            from PublisherJpaEntity p
            where p.logoFileAsset.id = :fileAssetId
            """)
    long countPublisherLogoUsages(@Param("fileAssetId") UUID fileAssetId);

    @Query("""
            select count(da)
            from DigitalAssetJpaEntity da
            where da.fileAsset.id = :fileAssetId
            """)
    long countDigitalAssetMainUsages(@Param("fileAssetId") UUID fileAssetId);

    @Query("""
            select count(da)
            from DigitalAssetJpaEntity da
            where da.sampleFileAsset.id = :fileAssetId
            """)
    long countDigitalAssetSampleUsages(@Param("fileAssetId") UUID fileAssetId);
}
