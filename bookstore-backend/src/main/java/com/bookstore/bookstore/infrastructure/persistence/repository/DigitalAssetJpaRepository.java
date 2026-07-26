package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DigitalAssetJpaRepository extends JpaRepository<DigitalAssetJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"fileAsset", "sampleFileAsset"})
    Optional<DigitalAssetJpaEntity> findByIdAndDeletedAtIsNullAndBook_DeletedAtIsNull(UUID digitalAssetId);

    @EntityGraph(attributePaths = {"fileAsset", "sampleFileAsset"})
    List<DigitalAssetJpaEntity> findAllByBook_IdAndDeletedAtIsNullAndBook_DeletedAtIsNullOrderByCreatedAtDesc(
            UUID bookId
    );

    @EntityGraph(attributePaths = {"fileAsset", "sampleFileAsset"})
    List<DigitalAssetJpaEntity> findAllByBook_IdOrderByCreatedAtDesc(UUID bookId);

    @EntityGraph(attributePaths = {"fileAsset", "sampleFileAsset"})
    List<DigitalAssetJpaEntity> findAllByBook_IdInAndDeletedAtIsNullAndBook_DeletedAtIsNullOrderByCreatedAtDesc(
            List<UUID> bookIds
    );

    @EntityGraph(attributePaths = {"fileAsset", "sampleFileAsset"})
    @Query("""
            select da
            from DigitalAssetJpaEntity da
            join da.book b
            join b.author a
            where da.deletedAt is null
              and da.published = true
              and b.deletedAt is null
              and b.category.deletedAt is null
              and b.author.deletedAt is null
              and b.publisher.deletedAt is null
              and (:categoryId is null or b.category.id = :categoryId)
              and (
                  :keyword is null
                  or lower(da.title) like lower(concat('%', :keyword, '%'))
                  or lower(coalesce(da.fileName, '')) like lower(concat('%', :keyword, '%'))
                  or lower(b.title) like lower(concat('%', :keyword, '%'))
                  or lower(coalesce(b.description, '')) like lower(concat('%', :keyword, '%'))
                  or lower(a.name) like lower(concat('%', :keyword, '%'))
              )
            order by da.createdAt desc
            """)
    Page<DigitalAssetJpaEntity> searchPublishedCatalog(
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"fileAsset", "sampleFileAsset"})
    List<DigitalAssetJpaEntity> findAllByIdInAndDeletedAtIsNullAndBook_DeletedAtIsNull(List<UUID> digitalAssetIds);

    @EntityGraph(attributePaths = {"fileAsset", "sampleFileAsset"})
    Optional<DigitalAssetJpaEntity> findById(UUID digitalAssetId);
}
