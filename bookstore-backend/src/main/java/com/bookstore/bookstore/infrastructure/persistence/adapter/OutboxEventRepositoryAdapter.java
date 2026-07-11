package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IOutboxEventRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.domain.model.OutboxEvent;
import com.bookstore.bookstore.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.OutboxEventPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.OutboxEventJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements IOutboxEventRepository {
    private final OutboxEventJpaRepository repository;
    private final OutboxEventPersistenceMapper mapper;
    @Override public Optional<OutboxEvent> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override public Optional<OutboxEvent> findByIdForUpdate(UUID id) { return repository.findByIdForUpdate(id).map(mapper::toDomain); }
    @Override public Optional<OutboxEvent> findByDeduplicationKey(String key) { return repository.findByDeduplicationKey(key).map(mapper::toDomain); }
    @Override public List<OutboxEvent> findClaimableForUpdate(Instant now, int limit) { return repository.findClaimableForUpdate(List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), now, PageRequest.of(0, limit)).stream().map(mapper::toDomain).toList(); }
    @Override public List<OutboxEvent> findStaleProcessingForUpdate(Instant before, int limit) { return repository.findStaleProcessingForUpdate(OutboxStatus.PROCESSING, before, PageRequest.of(0, limit)).stream().map(mapper::toDomain).toList(); }
    @Override public PageSliceResult<OutboxEvent> findPage(int page, int size, OutboxStatus status) {
        var result = status == null ? repository.findAll(PageRequest.of(page, size)) : repository.findAllByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
        return new PageSliceResult<>(result.getContent().stream().map(mapper::toDomain).toList(), result.getTotalElements(), page, size);
    }
    @Override public long countByStatus(OutboxStatus status) { return repository.countByStatus(status); }
    @Override public OutboxEvent save(OutboxEvent event) {
        OutboxEventJpaEntity entity = repository.findById(event.getId()).orElseGet(OutboxEventJpaEntity::new);
        mapper.copyToEntity(event, entity);
        return mapper.toDomain(repository.save(entity));
    }
}
