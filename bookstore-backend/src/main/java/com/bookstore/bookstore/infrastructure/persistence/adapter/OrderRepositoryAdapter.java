package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.OrderPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.CouponJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
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
    private final UserJpaRepository userJpaRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final OrderPersistenceMapper orderPersistenceMapper;

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findByIdAndUser_DeletedAtIsNull(orderId)
                .map(orderPersistenceMapper::toDomain);

    }

    @Override
    public List<Order> findByUserId(UUID userId) {
        return orderJpaRepository.findAllByUserIdAndUser_DeletedAtIsNull(userId).stream()
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
        return orderJpaRepository.findAllByUser_DeletedAtIsNull().stream()
                .map(orderPersistenceMapper::toDomain)
                .toList();

    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderJpaRepository.findByIdAndUser_DeletedAtIsNull(order.getId())
                .orElseGet(OrderJpaEntity::new);
        
        UserJpaEntity user = userJpaRepository.getReferenceById(order.getUserId());
        CouponJpaEntity bookCoupon = order.getBookCouponId() != null 
                ? couponJpaRepository.getReferenceById(order.getBookCouponId()) 
                : null;
        CouponJpaEntity shippingCoupon = order.getShippingCouponId() != null 
                ? couponJpaRepository.getReferenceById(order.getShippingCouponId()) 
                : null;
        
        orderPersistenceMapper.copyToEntity(order, entity, user, bookCoupon, shippingCoupon);
        return orderPersistenceMapper.toDomain(orderJpaRepository.save(entity));
    }
}
