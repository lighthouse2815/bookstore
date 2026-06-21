package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.ConversationStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.ConversationJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationJpaRepository extends JpaRepository<ConversationJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"customer", "assignedStaff"})
    List<ConversationJpaEntity> findAllByCustomer_IdAndDeletedAtIsNull(UUID customerId, Sort sort);

    @EntityGraph(attributePaths = {"customer", "assignedStaff"})
    Optional<ConversationJpaEntity> findByIdAndCustomer_IdAndDeletedAtIsNull(UUID conversationId, UUID customerId);

    @EntityGraph(attributePaths = {"customer", "assignedStaff"})
    Optional<ConversationJpaEntity> findByIdAndDeletedAtIsNull(UUID conversationId);

    @EntityGraph(attributePaths = {"customer", "assignedStaff"})
    @Query(
            value = """
                    select c
                    from ConversationJpaEntity c
                    join c.customer customer
                    left join c.assignedStaff assignedStaff
                    where c.deletedAt is null
                      and (:status is null or c.status = :status)
                      and (
                            :keyword is null
                            or lower(c.subject) like lower(concat('%', :keyword, '%'))
                            or lower(coalesce(c.lastMessagePreview, '')) like lower(concat('%', :keyword, '%'))
                            or lower(customer.username) like lower(concat('%', :keyword, '%'))
                            or lower(customer.email) like lower(concat('%', :keyword, '%'))
                            or lower(coalesce(assignedStaff.username, '')) like lower(concat('%', :keyword, '%'))
                            or lower(coalesce(assignedStaff.email, '')) like lower(concat('%', :keyword, '%'))
                      )
                    order by c.lastMessageAt desc, c.createdAt desc
                    """,
            countQuery = """
                    select count(c)
                    from ConversationJpaEntity c
                    join c.customer customer
                    left join c.assignedStaff assignedStaff
                    where c.deletedAt is null
                      and (:status is null or c.status = :status)
                      and (
                            :keyword is null
                            or lower(c.subject) like lower(concat('%', :keyword, '%'))
                            or lower(coalesce(c.lastMessagePreview, '')) like lower(concat('%', :keyword, '%'))
                            or lower(customer.username) like lower(concat('%', :keyword, '%'))
                            or lower(customer.email) like lower(concat('%', :keyword, '%'))
                            or lower(coalesce(assignedStaff.username, '')) like lower(concat('%', :keyword, '%'))
                            or lower(coalesce(assignedStaff.email, '')) like lower(concat('%', :keyword, '%'))
                      )
                    """
    )
    Page<ConversationJpaEntity> searchActive(
            @Param("status") ConversationStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
