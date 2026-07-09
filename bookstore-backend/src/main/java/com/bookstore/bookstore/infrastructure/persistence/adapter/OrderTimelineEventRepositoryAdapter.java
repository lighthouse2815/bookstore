package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IOrderTimelineEventRepository;
import com.bookstore.bookstore.domain.model.OrderTimelineEvent;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderTimelineEventJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.OrderTimelineEventPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderTimelineEventJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class OrderTimelineEventRepositoryAdapter implements IOrderTimelineEventRepository {

    private final OrderTimelineEventJpaRepository orderTimelineEventJpaRepository;
    private final OrderTimelineEventPersistenceMapper orderTimelineEventPersistenceMapper;
    private final OrderJpaRepository orderJpaRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderTimelineEvent save(OrderTimelineEvent event) {
        OrderTimelineEventJpaEntity entity = orderTimelineEventJpaRepository.findById(event.getId())
                .orElseGet(OrderTimelineEventJpaEntity::new);
        OrderJpaEntity order = orderJpaRepository.getReferenceById(event.getOrderId());
        orderTimelineEventPersistenceMapper.copyToEntity(event, entity, order);
        return orderTimelineEventPersistenceMapper.toDomain(orderTimelineEventJpaRepository.save(entity));
    }

    @Override
    public List<OrderTimelineEvent> findByOrderId(java.util.UUID orderId) {
        return orderTimelineEventJpaRepository.findAllByOrder_IdOrderByCreatedAtAscIdAsc(orderId).stream()
                .map(orderTimelineEventPersistenceMapper::toDomain)
                .toList();
    }
}
