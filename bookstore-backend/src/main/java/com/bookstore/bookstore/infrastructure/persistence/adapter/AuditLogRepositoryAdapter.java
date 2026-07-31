package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IAuditLogRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.AuditAction;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.domain.model.AuditLog;
import com.bookstore.bookstore.infrastructure.persistence.entity.AuditLogJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.AuditLogPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.AuditLogJpaRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements IAuditLogRepository {

    private final AuditLogJpaRepository auditLogJpaRepository;
    private final AuditLogPersistenceMapper auditLogPersistenceMapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpaEntity entity = auditLogJpaRepository.findById(auditLog.getId())
                .orElseGet(AuditLogJpaEntity::new);
        auditLogPersistenceMapper.copyToEntity(entity, auditLog);
        return auditLogPersistenceMapper.toDomain(auditLogJpaRepository.save(entity));
    }

    @Override
    public PageSliceResult<AuditLog> findPage(
            int page,
            int size,
            AuditAction action,
            AuditTargetType targetType,
            UUID actorId,
            Instant from,
            Instant to
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = auditLogJpaRepository.findAll(buildSpecification(action, targetType, actorId, from, to), pageable);
        return new PageSliceResult<>(
                result.getContent().stream()
                        .map(auditLogPersistenceMapper::toDomain)
                        .toList(),
                result.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public Optional<AuditLog> findById(UUID auditLogId) {
        return auditLogJpaRepository.findById(auditLogId)
                .map(auditLogPersistenceMapper::toDomain);
    }

    private Specification<AuditLogJpaEntity> buildSpecification(
            AuditAction action,
            AuditTargetType targetType,
            UUID actorId,
            Instant from,
            Instant to
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (action != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            if (targetType != null) {
                predicates.add(criteriaBuilder.equal(root.get("targetType"), targetType));
            }
            if (actorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("actorId"), actorId));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
