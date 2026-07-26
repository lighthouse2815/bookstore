package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IPaymentReconciliationIssueRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.domain.model.PaymentReconciliationIssue;
import com.bookstore.bookstore.infrastructure.persistence.entity.PaymentReconciliationIssueJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.PaymentReconciliationIssuePersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.PaymentReconciliationIssueJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentReconciliationIssueRepositoryAdapter implements IPaymentReconciliationIssueRepository {
    private final PaymentReconciliationIssueJpaRepository repository;
    private final PaymentReconciliationIssuePersistenceMapper mapper;
    @Override public boolean existsByDeduplicationKey(String key) { return repository.existsByDeduplicationKey(key); }
    @Override public Optional<PaymentReconciliationIssue> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override public PaymentReconciliationIssue save(PaymentReconciliationIssue issue) {
        PaymentReconciliationIssueJpaEntity entity = repository.findById(issue.getId()).orElseGet(PaymentReconciliationIssueJpaEntity::new);
        mapper.copyToEntity(issue, entity);
        return mapper.toDomain(repository.save(entity));
    }
    @Override public PageSliceResult<PaymentReconciliationIssue> findPage(
            int page, int size, PaymentReconciliationStatus status, PaymentReconciliationIssueType issueType, Instant from, Instant to
    ) {
        var result = repository.findPage(status, issueType, from, to, PageRequest.of(page, size));
        return new PageSliceResult<>(result.getContent().stream().map(mapper::toDomain).toList(), result.getTotalElements(), page, size);
    }
}
