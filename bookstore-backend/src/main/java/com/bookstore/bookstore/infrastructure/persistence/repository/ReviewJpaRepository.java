package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReviewJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.projection.ReviewReportProjection;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, UUID> {

    @Query("""
            select r
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.book.deletedAt is null
              and r.user.deletedAt is null
              and r.book.id = :bookId
              and r.status = :status
            order by r.createdAt desc
            """)
    List<ReviewJpaEntity> findAllByBook_IdAndStatusActive(
            @Param("bookId") UUID bookId,
            @Param("status") ReviewStatus status
    );

    @Query("""
            select r
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.book.deletedAt is null
              and r.user.deletedAt is null
              and r.book.id = :bookId
              and r.status = :status
            order by r.createdAt desc
            """)
    Page<ReviewJpaEntity> findAllByBook_IdAndStatusActive(
            @Param("bookId") UUID bookId,
            @Param("status") ReviewStatus status,
            Pageable pageable
    );

    @Query("""
            select r.book.id, r.rating
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.book.deletedAt is null
              and r.user.deletedAt is null
              and r.book.id in :bookIds
              and r.status = :status
            """)
    List<Object[]> findRatingsByBookIdsAndStatus(
            @Param("bookIds") Collection<UUID> bookIds,
            @Param("status") ReviewStatus status
    );

    @Query("""
            select r
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.book.deletedAt is null
              and r.user.deletedAt is null
              and (:status is null or r.status = :status)
              and (:bookId is null or r.book.id = :bookId)
              and (:userId is null or r.user.id = :userId)
              and (:rating is null or r.rating = :rating)
            order by r.updatedAt desc, r.createdAt desc
            """)
    Page<ReviewJpaEntity> findPageActive(
            @Param("status") ReviewStatus status,
            @Param("bookId") UUID bookId,
            @Param("userId") UUID userId,
            @Param("rating") Integer rating,
            Pageable pageable
    );

    List<ReviewJpaEntity> findAllByDeletedAtIsNullAndBook_DeletedAtIsNullAndUser_DeletedAtIsNullOrderByCreatedAtDesc();

    Page<ReviewJpaEntity> findAllByDeletedAtIsNullAndBook_DeletedAtIsNullAndUser_DeletedAtIsNullOrderByCreatedAtDesc(
            Pageable pageable
    );

    Optional<ReviewJpaEntity> findByIdAndDeletedAtIsNullAndBook_DeletedAtIsNullAndUser_DeletedAtIsNull(UUID reviewId);

    Optional<ReviewJpaEntity> findByIdAndUser_IdAndDeletedAtIsNullAndUser_DeletedAtIsNull(UUID reviewId, UUID userId);

    Optional<ReviewJpaEntity> findById(UUID reviewId);

    boolean existsByOrderItemId(UUID orderItemId);

    @Query("""
            select count(r)
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.book.deletedAt is null
              and r.user.deletedAt is null
              and r.createdAt >= :fromInclusive
              and r.createdAt < :toExclusive
            """)
    long countNewReviewsBetween(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );

    @Query("""
            select r.book.title as bookTitle,
                   r.user.username as username,
                   r.rating as rating,
                   r.status as status,
                   r.createdAt as createdAt,
                   r.moderationReason as moderationReason
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.book.deletedAt is null
              and r.user.deletedAt is null
              and (:status is null or r.status = :status)
            order by r.createdAt desc, r.id desc
            """)
    List<ReviewReportProjection> findReviewReportRows(@Param("status") ReviewStatus status);
}
