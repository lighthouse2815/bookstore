package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ReviewJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
            order by r.createdAt desc
            """)
    List<ReviewJpaEntity> findAllByBook_IdActive(@Param("bookId") UUID bookId);

    @Query("""
            select r.book.id, r.rating
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.book.deletedAt is null
              and r.user.deletedAt is null
              and r.book.id in :bookIds
            """)
    List<Object[]> findRatingsByBookIds(@Param("bookIds") Collection<UUID> bookIds);

    List<ReviewJpaEntity> findAllByDeletedAtIsNullAndBook_DeletedAtIsNullAndUser_DeletedAtIsNullOrderByCreatedAtDesc();

    Optional<ReviewJpaEntity> findByIdAndDeletedAtIsNullAndBook_DeletedAtIsNullAndUser_DeletedAtIsNull(UUID reviewId);

    Optional<ReviewJpaEntity> findByIdAndUser_IdAndDeletedAtIsNullAndUser_DeletedAtIsNull(UUID reviewId, UUID userId);

    Optional<ReviewJpaEntity> findById(UUID reviewId);

    boolean existsByOrderItemId(UUID orderItemId);
}
