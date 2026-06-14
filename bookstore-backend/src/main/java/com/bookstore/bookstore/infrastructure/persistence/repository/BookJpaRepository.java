package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"images", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.deletedAt is null
            """)
    List<BookJpaEntity> findAllActive();

    @EntityGraph(attributePaths = {"images", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            """)
    List<BookJpaEntity> findAllIncludingDeleted();

    @EntityGraph(attributePaths = {"images", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.deletedAt is null
              and b.id = :id
            """)
    Optional<BookJpaEntity> findByIdActive(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"images", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.id = :id
            """)
    Optional<BookJpaEntity> findByIdIncludingDeleted(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"images", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.id in :bookIds
            """)
    List<BookJpaEntity> findAllByIdsIncludingDeleted(@Param("bookIds") Collection<UUID> bookIds);

    @EntityGraph(attributePaths = {"images", "detail"})
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

    @EntityGraph(attributePaths = {"images", "detail"})
    @Query("""
            select distinct b
            from BookJpaEntity b
            where b.deletedAt is null
              and b.categoryId = :categoryId
              and b.id <> :excludedBookId
            order by b.createdAt desc
            """)
    List<BookJpaEntity> findRelatedActiveByCategoryId(
            @Param("categoryId") UUID categoryId,
            @Param("excludedBookId") UUID excludedBookId,
            Pageable pageable
    );
}
