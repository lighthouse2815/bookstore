package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements IOrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findDetailedById(orderId)
                .map(orderPersistenceMapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(UUID userId) {
        return orderJpaRepository.findAllByUserId(userId).stream()
                .map(orderPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, Long> countDeliveredQuantityByBookIds(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        return orderJpaRepository.countDeliveredQuantityByBookIds(bookIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAllDetailed().stream()
                .map(orderPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderJpaRepository.findDetailedById(order.getId())
                .orElseGet(OrderJpaEntity::new);
        orderPersistenceMapper.copyToEntity(order, entity);
        return orderPersistenceMapper.toDomain(orderJpaRepository.save(entity));
    }
}
