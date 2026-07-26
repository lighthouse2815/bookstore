package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CouponJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
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
                entity.getUser().getId(),
                entity.getItems().stream()
                        .map(this::toDomain)
                        .toList(),
                resolveProductTotal(entity),
                entity.getShippingFee(),
                resolveShippingDiscount(entity),
                resolveCouponDiscount(entity),
                resolveTotalAmount(entity),
                resolveBookCouponId(entity),
                entity.getBookCouponCode(),
                resolveShippingCouponId(entity),
                entity.getShippingCouponCode(),
                entity.getPaymentMethod(),
                entity.getPaymentStatus(),
                entity.getStatus(),
                entity.getReceiverName(),
                entity.getReceiverPhone(),
                entity.getReceiverAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCancelledAt(),
                entity.getIdempotencyKey(),
                entity.getCheckoutFingerprint()
        );
    }

    public void copyToEntityWithReferences(
            Order order,
            OrderJpaEntity entity,
            UserJpaEntity user,
            CouponJpaEntity bookCoupon,
            CouponJpaEntity shippingCoupon,
            Map<UUID, BookJpaEntity> bookMap,
            Map<UUID, DigitalAssetJpaEntity> digitalAssetMap
    ) {
        entity.setId(order.getId());
        entity.setOrderCode(order.getOrderCode());
        entity.setUser(user);
        entity.setProductTotal(order.getProductTotal());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setDiscountAmount(order.getDiscountAmount());
        entity.setShippingFee(order.getShippingFee());
        entity.setShippingDiscount(order.getShippingDiscount());
        entity.setCouponDiscount(order.getCouponDiscount());
        entity.setFinalAmount(order.getFinalAmount());
        entity.setBookCoupon(bookCoupon);
        entity.setBookCouponCode(order.getBookCouponCode());
        entity.setShippingCoupon(shippingCoupon);
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
        entity.setIdempotencyKey(order.getIdempotencyKey());
        entity.setCheckoutFingerprint(order.getCheckoutFingerprint());

        Map<UUID, OrderItemJpaEntity> currentItems = entity.getItems().stream()
                .collect(Collectors.toMap(OrderItemJpaEntity::getId, Function.identity()));

        var mappedItems = order.getItems().stream()
                .map(item -> {
                    OrderItemJpaEntity itemEntity = currentItems.getOrDefault(item.getId(), new OrderItemJpaEntity());
                    copyItemToEntity(
                            item,
                            itemEntity,
                            entity,
                            bookMap.get(item.getBookId()),
                            item.getDigitalAssetId() == null ? null : digitalAssetMap.get(item.getDigitalAssetId())
                    );
                    return itemEntity;
                })
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(mappedItems);
    }

    private OrderItem toDomain(OrderItemJpaEntity entity) {
        return new OrderItem(
                entity.getId(),
                entity.getItemType() == null ? PurchaseItemType.PHYSICAL_BOOK : entity.getItemType(),
                entity.getBook().getId(),
                entity.getDigitalAsset() == null ? null : entity.getDigitalAsset().getId(),
                entity.getBookTitle(),
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getLineTotal()
        );
    }

    private void copyItemToEntity(
            OrderItem item,
            OrderItemJpaEntity entity,
            OrderJpaEntity orderEntity,
            BookJpaEntity book,
            DigitalAssetJpaEntity digitalAsset
    ) {
        entity.setId(item.getId());
        entity.setOrder(orderEntity);
        entity.setItemType(item.getItemType());
        entity.setBook(book);
        entity.setDigitalAsset(digitalAsset);
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
        return entity.getBookCoupon() == null ? null : entity.getBookCoupon().getId();
    }

    private UUID resolveShippingCouponId(OrderJpaEntity entity) {
        return entity.getShippingCoupon() == null ? null : entity.getShippingCoupon().getId();
    }
}
