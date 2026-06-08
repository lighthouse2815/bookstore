package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ImportReceiptJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImportReceiptJpaRepository extends JpaRepository<ImportReceiptJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    @Query("""
            select ir
            from ImportReceiptJpaEntity ir
            where ir.id = :receiptId
            """)
    Optional<ImportReceiptJpaEntity> findDetailedById(@Param("receiptId") UUID receiptId);

    @EntityGraph(attributePaths = "items")
    @Query("""
            select ir
            from ImportReceiptJpaEntity ir
            order by ir.createdAt desc
            """)
    List<ImportReceiptJpaEntity> findAllDetailed();
}
