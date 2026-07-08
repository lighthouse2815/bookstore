package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.WishlistItemJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistItemJpaRepository extends JpaRepository<WishlistItemJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"user", "book"})
    @Query("""
            select w
            from WishlistItemJpaEntity w
            where w.user.id = :userId
              and w.deletedAt is null
            order by w.createdAt desc, w.id desc
            """)
    List<WishlistItemJpaEntity> findAllByUserIdActive(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"user", "book"})
    @Query("""
            select w
            from WishlistItemJpaEntity w
            where w.user.id = :userId
              and w.book.id = :bookId
            """)
    Optional<WishlistItemJpaEntity> findByUserIdAndBookId(
            @Param("userId") UUID userId,
            @Param("bookId") UUID bookId
    );
}
