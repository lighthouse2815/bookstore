package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookshelfJpaRepository extends JpaRepository<BookshelfJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            select b
            from BookshelfJpaEntity b
            where b.user.id = :userId
              and b.deletedAt is null
            order by b.updatedAt desc, b.createdAt desc, b.id desc
            """)
    List<BookshelfJpaEntity> findAllByUserIdActive(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            select b
            from BookshelfJpaEntity b
            where b.id = :shelfId
              and b.user.id = :userId
              and b.deletedAt is null
            """)
    Optional<BookshelfJpaEntity> findByIdAndUserIdActive(
            @Param("shelfId") UUID shelfId,
            @Param("userId") UUID userId
    );

    @EntityGraph(attributePaths = {"user"})
    @Query("""
            select b
            from BookshelfJpaEntity b
            where b.user.id = :userId
              and b.name = :name
            """)
    Optional<BookshelfJpaEntity> findByUserIdAndName(
            @Param("userId") UUID userId,
            @Param("name") String name
    );
}
