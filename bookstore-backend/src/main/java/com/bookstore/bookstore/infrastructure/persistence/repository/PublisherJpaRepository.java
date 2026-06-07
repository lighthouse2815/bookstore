package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublisherJpaRepository extends JpaRepository<PublisherJpaEntity, UUID> {

    @Query("""
            select p
            from PublisherJpaEntity p
            where p.deletedAt is null
            """)
    List<PublisherJpaEntity> findAllActive();

    @Query("""
            select p
            from PublisherJpaEntity p
            """)
    List<PublisherJpaEntity> findAllIncludingDeleted();

    @Query("""
            select p
            from PublisherJpaEntity p
            where p.deletedAt is null
              and p.id = :id
            """)
    Optional<PublisherJpaEntity> findByIdActive(@Param("id") UUID id);

    @Query("""
            select p
            from PublisherJpaEntity p
            where p.id = :id
            """)
    Optional<PublisherJpaEntity> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select case when count(p) > 0 then true else false end
            from PublisherJpaEntity p
            where p.id = :id
            """)
    boolean existsByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select p
            from PublisherJpaEntity p
            where p.deletedAt is null
              and p.name = :name
            """)
    Optional<PublisherJpaEntity> findByNameActive(@Param("name") String name);

    @Query("""
            select case when count(p) > 0 then true else false end
            from PublisherJpaEntity p
            where p.name = :name
            """)
    boolean existsByNameIncludingDeleted(@Param("name") String name);
}
