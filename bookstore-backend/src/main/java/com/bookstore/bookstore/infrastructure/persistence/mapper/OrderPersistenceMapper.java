package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Order(
                entity.getId(),
                resolveOrderCode(entity),
                entity.getUserId(),
                entity.getItems().stream()
                        .map(this::toDomain)
                        .toList(),
                resolveProductTotal(entity),
                entity.getShippingFee(),
                resolveShippingDiscount(entity),
                resolveCouponDiscount(entity),
                resolveTotalAmount(entity),
                resolveBookCouponId(entity),
                resolveBookCouponCode(entity),
                entity.getShippingCouponId(),
                entity.getShippingCouponCode(),
                entity.getPaymentMethod(),
                entity.getPaymentStatus(),
                entity.getStatus(),
                entity.getReceiverName(),
                entity.getReceiverPhone(),
                entity.getReceiverAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCancelledAt()
        );
    }

    public void copyToEntity(Order order, OrderJpaEntity entity) {
        entity.setId(order.getId());
        entity.setOrderCode(order.getOrderCode());
        entity.setUserId(order.getUserId());
        entity.setProductTotal(order.getProductTotal());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setDiscountAmount(order.getDiscountAmount());
        entity.setShippingFee(order.getShippingFee());
        entity.setShippingDiscount(order.getShippingDiscount());
        entity.setCouponDiscount(order.getCouponDiscount());
        entity.setFinalAmount(order.getFinalAmount());
        entity.setCouponId(order.getCouponId());
        entity.setCouponCode(order.getCouponCode());
        entity.setBookCouponId(order.getBookCouponId());
        entity.setBookCouponCode(order.getBookCouponCode());
        entity.setShippingCouponId(order.getShippingCouponId());
        entity.setShippingCouponCode(order.getShippingCouponCode());
        entity.setPaymentMethod(order.getPaymentMethod());
        entity.setPaymentStatus(order.getPaymentStatus());
        entity.setStatus(order.getStatus());
        entity.setReceiverName(order.getReceiverName());
        entity.setReceiverPhone(order.getReceiverPhone());
        entity.setReceiverAddress(order.getReceiverAddress());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        entity.setCancelledAt(order.getCancelledAt());

        Map<UUID, OrderItemJpaEntity> currentItems = entity.getItems().stream()
                .collect(Collectors.toMap(OrderItemJpaEntity::getId, Function.identity()));

        var mappedItems = order.getItems().stream()
                .map(item -> {
                    OrderItemJpaEntity itemEntity = currentItems.getOrDefault(item.getId(), new OrderItemJpaEntity());
                    copyItemToEntity(item, itemEntity, entity);
                    return itemEntity;
                })
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(mappedItems);
    }

    private OrderItem toDomain(OrderItemJpaEntity entity) {
        return new OrderItem(
                entity.getId(),
                entity.getBookId(),
                entity.getBookTitle(),
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getLineTotal()
        );
    }

    private void copyItemToEntity(OrderItem item, OrderItemJpaEntity entity, OrderJpaEntity orderEntity) {
        entity.setId(item.getId());
        entity.setOrder(orderEntity);
        entity.setBookId(item.getBookId());
        entity.setBookTitle(item.getBookTitle());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setQuantity(item.getQuantity());
        entity.setLineTotal(item.getLineTotal());
    }

    private String resolveOrderCode(OrderJpaEntity entity) {
        return entity.getOrderCode() == null ? entity.getId().toString() : entity.getOrderCode();
    }

    private BigDecimal resolveProductTotal(OrderJpaEntity entity) {
        return entity.getProductTotal() == null ? entity.getTotalAmount() : entity.getProductTotal();
    }

    private BigDecimal resolveShippingDiscount(OrderJpaEntity entity) {
        return entity.getShippingDiscount() == null ? BigDecimal.ZERO : entity.getShippingDiscount();
    }

    private BigDecimal resolveCouponDiscount(OrderJpaEntity entity) {
        return entity.getCouponDiscount() == null ? entity.getDiscountAmount() : entity.getCouponDiscount();
    }

    private BigDecimal resolveTotalAmount(OrderJpaEntity entity) {
        if (entity.getProductTotal() == null && entity.getFinalAmount() != null) {
            return entity.getFinalAmount();
        }
        return entity.getTotalAmount();
    }

    private UUID resolveBookCouponId(OrderJpaEntity entity) {
        return entity.getBookCouponId() == null ? entity.getCouponId() : entity.getBookCouponId();
    }

    private String resolveBookCouponCode(OrderJpaEntity entity) {
        return entity.getBookCouponCode() == null ? entity.getCouponCode() : entity.getBookCouponCode();
    }
}
