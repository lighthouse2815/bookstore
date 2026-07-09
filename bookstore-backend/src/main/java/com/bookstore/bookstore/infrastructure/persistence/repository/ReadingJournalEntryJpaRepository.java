package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ReadingJournalEntryJpaEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadingJournalEntryJpaRepository extends JpaRepository<ReadingJournalEntryJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"user", "book"})
    @Query(
            value = """
                    select e
                    from ReadingJournalEntryJpaEntity e
                    where e.user.id = :userId
                      and e.deletedAt is null
                      and (:bookId is null or e.book.id = :bookId)
                      and (:from is null or e.entryDate >= :from)
                      and (:to is null or e.entryDate <= :to)
                    order by e.entryDate desc, e.updatedAt desc, e.id desc
                    """,
            countQuery = """
                    select count(e)
                    from ReadingJournalEntryJpaEntity e
                    where e.user.id = :userId
                      and e.deletedAt is null
                      and (:bookId is null or e.book.id = :bookId)
                      and (:from is null or e.entryDate >= :from)
                      and (:to is null or e.entryDate <= :to)
                    """
    )
    Page<ReadingJournalEntryJpaEntity> findPageByUserId(
            @Param("userId") UUID userId,
            @Param("bookId") UUID bookId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "book"})
    @Query("""
            select e
            from ReadingJournalEntryJpaEntity e
            where e.id = :entryId
              and e.user.id = :userId
              and e.deletedAt is null
            """)
    Optional<ReadingJournalEntryJpaEntity> findByIdAndUserIdActive(
            @Param("entryId") UUID entryId,
            @Param("userId") UUID userId
    );

    @EntityGraph(attributePaths = {"user", "book"})
    @Query("""
            select e
            from ReadingJournalEntryJpaEntity e
            where e.user.id = :userId
              and e.book.id = :bookId
              and e.entryDate = :entryDate
            """)
    Optional<ReadingJournalEntryJpaEntity> findByUserIdAndBookIdAndEntryDate(
            @Param("userId") UUID userId,
            @Param("bookId") UUID bookId,
            @Param("entryDate") LocalDate entryDate
    );

    @Query("""
            select distinct e.entryDate
            from ReadingJournalEntryJpaEntity e
            where e.user.id = :userId
              and e.deletedAt is null
            order by e.entryDate desc
            """)
    List<LocalDate> findDistinctEntryDatesByUserIdActive(@Param("userId") UUID userId);
}
