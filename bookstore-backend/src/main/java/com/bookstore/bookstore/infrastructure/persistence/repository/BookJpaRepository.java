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
    List<BookJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = {"images", "detail"})
    List<BookJpaEntity> findAll();

    @EntityGraph(attributePaths = {"images", "detail"})
    Optional<BookJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    @EntityGraph(attributePaths = {"images", "detail"})
    Optional<BookJpaEntity> findById(UUID id);

    @EntityGraph(attributePaths = {"images", "detail"})
    List<BookJpaEntity> findAllByIdIn(Collection<UUID> bookIds);
    
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
}
