package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IRefundRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.domain.model.Refund;
import com.bookstore.bookstore.infrastructure.persistence.entity.RefundJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.RefundPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.RefundJpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefundRepositoryAdapter implements IRefundRepository {
    private final RefundJpaRepository repository;
    private final RefundPersistenceMapper mapper;
    @Override public Optional<Refund> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override public Optional<Refund> findByIdForUpdate(UUID id) { return repository.findByIdForUpdate(id).map(mapper::toDomain); }
    @Override public Optional<Refund> findByOrderIdAndIdempotencyKey(UUID orderId, String key) { return repository.findByOrderIdAndIdempotencyKey(orderId, key).map(mapper::toDomain); }
    @Override public PageSliceResult<Refund> findPage(int page, int size, RefundStatus status, RefundMethod method, Instant from, Instant to) {
        var result = repository.findPage(status, method, from, to, PageRequest.of(page, size));
        return new PageSliceResult<>(result.getContent().stream().map(mapper::toDomain).toList(), result.getTotalElements(), page, size);
    }
    @Override public BigDecimal sumAmountByPaymentIdAndStatuses(UUID paymentId, Collection<RefundStatus> statuses) {
        BigDecimal value = repository.sumAmountByPaymentIdAndStatuses(paymentId, statuses);
        return value == null ? BigDecimal.ZERO : value;
    }
    @Override public Refund save(Refund refund) {
        RefundJpaEntity entity = repository.findById(refund.getId()).orElseGet(RefundJpaEntity::new);
        mapper.copyToEntity(refund, entity);
        return mapper.toDomain(repository.save(entity));
    }
}
