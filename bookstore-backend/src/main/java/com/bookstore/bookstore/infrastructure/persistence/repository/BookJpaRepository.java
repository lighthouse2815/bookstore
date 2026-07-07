package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.projection.LowStockBookProjection;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    List<BookJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    Page<BookJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    List<BookJpaEntity> findAll();

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    Optional<BookJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    Optional<BookJpaEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    List<BookJpaEntity> findAllByIdIn(Collection<UUID> bookIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.id in :bookIds
            order by b.id
            """)
    List<BookJpaEntity> findAllByIdInForUpdate(@Param("bookIds") Collection<UUID> bookIds);
    
    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.deletedAt is null
              and (
                  lower(b.title) like lower(concat('%', :keyword, '%'))
                  or lower(coalesce(b.description, '')) like lower(concat('%', :keyword, '%'))
              )
            """)
    List<BookJpaEntity> searchByKeywordActive(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.deletedAt is null
              and (
                  lower(b.title) like lower(concat('%', :keyword, '%'))
                  or lower(coalesce(b.description, '')) like lower(concat('%', :keyword, '%'))
              )
            order by b.createdAt desc
            """)
    Page<BookJpaEntity> searchByKeywordActive(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.deletedAt is null
              and (:categoryId is null or b.category.id = :categoryId)
              and (
                  :keyword is null
                  or lower(b.title) like lower(concat('%', :keyword, '%'))
                  or lower(coalesce(b.description, '')) like lower(concat('%', :keyword, '%'))
                  or lower(b.author.name) like lower(concat('%', :keyword, '%'))
              )
            order by b.createdAt desc
            """)
    Page<BookJpaEntity> searchActive(
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"images", "images.fileAsset", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.deletedAt is null
              and b.category.deletedAt is null
              and b.author.deletedAt is null
              and b.publisher.deletedAt is null
              and b.category.id = :categoryId
              and b.id <> :excludedBookId
            order by b.createdAt desc
            """)
    List<BookJpaEntity> findRelatedActiveByCategoryId(
            @Param("categoryId") UUID categoryId,
            @Param("excludedBookId") UUID excludedBookId,
            Pageable pageable
    );

    long countByDeletedAtIsNullAndStockQuantityLessThanEqual(int threshold);

    @Query("""
            select b.id as bookId,
                   b.title as title,
                   b.stockQuantity as stockQuantity
            from BookJpaEntity b
            where b.deletedAt is null
              and b.stockQuantity <= :threshold
            order by b.stockQuantity asc, b.createdAt asc
            """)
    List<LowStockBookProjection> findLowStockBooks(@Param("threshold") int threshold);
}
