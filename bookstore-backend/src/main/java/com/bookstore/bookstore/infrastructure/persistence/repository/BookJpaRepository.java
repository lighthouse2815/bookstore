package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, UUID> {

    @Query("""
            select b
            from BookJpaEntity b
            where b.deletedAt is null
            """)
    List<BookJpaEntity> findAllActive();

    @Query("""
            select b
            from BookJpaEntity b
            """)
    List<BookJpaEntity> findAllIncludingDeleted();

    @Query("""
            select b
            from BookJpaEntity b
            where b.deletedAt is null
              and b.id = :id
            """)
    Optional<BookJpaEntity> findByIdActive(@Param("id") UUID id);

    @Query("""
            select b
            from BookJpaEntity b
            where b.id = :id
            """)
    Optional<BookJpaEntity> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select b
            from BookJpaEntity b
            where b.deletedAt is null
              and (
                  lower(b.title) like lower(concat('%', :keyword, '%'))
                  or lower(coalesce(b.description, '')) like lower(concat('%', :keyword, '%'))
              )
            """)
    List<BookJpaEntity> searchByKeywordActive(@Param("keyword") String keyword);
}
