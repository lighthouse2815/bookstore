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
              and r.bookId = :bookId
            order by r.createdAt desc
            """)
    List<ReviewJpaEntity> findAllByBookIdActive(@Param("bookId") UUID bookId);

    @Query("""
            select r.bookId, r.rating
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.bookId in :bookIds
            """)
    List<Object[]> findRatingsByBookIds(@Param("bookIds") Collection<UUID> bookIds);

    @Query("""
            select r
            from ReviewJpaEntity r
            where r.deletedAt is null
            order by r.createdAt desc
            """)
    List<ReviewJpaEntity> findAllActive();

    @Query("""
            select r
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.id = :reviewId
            """)
    Optional<ReviewJpaEntity> findByIdActive(@Param("reviewId") UUID reviewId);

    @Query("""
            select r
            from ReviewJpaEntity r
            where r.deletedAt is null
              and r.id = :reviewId
              and r.userId = :userId
            """)
    Optional<ReviewJpaEntity> findByIdAndUserIdActive(
            @Param("reviewId") UUID reviewId,
            @Param("userId") UUID userId
    );

    @Query("""
            select r
            from ReviewJpaEntity r
            where r.id = :reviewId
            """)
    Optional<ReviewJpaEntity> findByIdIncludingDeleted(@Param("reviewId") UUID reviewId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from ReviewJpaEntity r
            where r.orderItemId = :orderItemId
            """)
    boolean existsByOrderItemIdIncludingDeleted(@Param("orderItemId") UUID orderItemId);
}
