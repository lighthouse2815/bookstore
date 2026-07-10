package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfItemJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookshelfItemJpaRepository extends JpaRepository<BookshelfItemJpaEntity, UUID> {

    @Query("""
            select count(i)
            from BookshelfItemJpaEntity i
            where i.shelf.id = :shelfId
              and i.deletedAt is null
            """)
    long countActiveByShelfId(@Param("shelfId") UUID shelfId);

    @Query("""
            select i.shelf.id, count(i)
            from BookshelfItemJpaEntity i
            where i.shelf.id in :shelfIds
              and i.deletedAt is null
            group by i.shelf.id
            """)
    List<Object[]> countActiveByShelfIds(@Param("shelfIds") Collection<UUID> shelfIds);

    @EntityGraph(attributePaths = {"shelf", "book"})
    @Query("""
            select i
            from BookshelfItemJpaEntity i
            where i.shelf.id = :shelfId
              and i.deletedAt is null
            order by i.sortOrder asc, i.createdAt asc, i.id asc
            """)
    List<BookshelfItemJpaEntity> findAllByShelfIdActive(@Param("shelfId") UUID shelfId);

    @EntityGraph(attributePaths = {"shelf", "book"})
    @Query("""
            select i
            from BookshelfItemJpaEntity i
            where i.shelf.id in :shelfIds
              and i.shelf.deletedAt is null
              and i.deletedAt is null
            order by i.shelf.id, i.sortOrder asc, i.createdAt asc, i.id asc
            """)
    List<BookshelfItemJpaEntity> findAllByShelfIdsActive(@Param("shelfIds") Collection<UUID> shelfIds);

    @EntityGraph(attributePaths = {"shelf", "book"})
    @Query("""
            select i
            from BookshelfItemJpaEntity i
            where i.shelf.id = :shelfId
              and i.book.id = :bookId
            """)
    Optional<BookshelfItemJpaEntity> findByShelfIdAndBookId(
            @Param("shelfId") UUID shelfId,
            @Param("bookId") UUID bookId
    );
}
