package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.AuthorJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorJpaRepository extends JpaRepository<AuthorJpaEntity, UUID> {

    @Query("""
            select a
            from AuthorJpaEntity a
            where a.deletedAt is null
            """)
    List<AuthorJpaEntity> findAllActive();

    @Query("""
            select a
            from AuthorJpaEntity a
            """)
    List<AuthorJpaEntity> findAllIncludingDeleted();

    @Query("""
            select a
            from AuthorJpaEntity a
            where a.deletedAt is null
              and a.id = :id
            """)
    Optional<AuthorJpaEntity> findByIdActive(@Param("id") UUID id);

    @Query("""
            select a
            from AuthorJpaEntity a
            where a.id = :id
            """)
    Optional<AuthorJpaEntity> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select case when count(a) > 0 then true else false end
            from AuthorJpaEntity a
            where a.id = :id
            """)
    boolean existsByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select a
            from AuthorJpaEntity a
            where a.deletedAt is null
              and a.name = :name
            """)
    Optional<AuthorJpaEntity> findByNameActive(@Param("name") String name);

    @Query("""
            select case when count(a) > 0 then true else false end
            from AuthorJpaEntity a
            where a.name = :name
            """)
    boolean existsByNameIncludingDeleted(@Param("name") String name);
}
