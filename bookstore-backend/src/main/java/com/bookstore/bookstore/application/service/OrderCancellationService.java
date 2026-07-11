package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.in.ITransactionalOutboxService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.application.port.out.ICouponUsageRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.StockMovement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Cancellation lock order is always payment, order, books (ascending IDs), then coupons.
 */
@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private final IPaymentRepository paymentRepository;
    private final IOrderRepository orderRepository;
    private final IBookRepository bookRepository;
    private final ICouponRepository couponRepository;
    private final ICouponUsageRepository couponUsageRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final ITransactionalOutboxService transactionalOutboxService;
    private final IOrderTimelineService orderTimelineService;
    private final IDigitalLibraryService digitalLibraryService;
    private final IAuditLogService auditLogService;

    public Order cancelOwnedPending(UUID userId, UUID orderId, String reason) {
        LockedOrderPayment locked = lockPaymentThenOrder(orderId);
        if (!userId.equals(locked.order().getUserId())) {
            throw new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND);
        }
        return cancelLocked(locked.order(), locked.payment(), PaymentStatus.CANCELLED, reason, userId, "ORDER_CANCELLED_BY_USER");
    }

    public Order cancelPendingByAdmin(UUID orderId, String reason) {
        LockedOrderPayment locked = lockPaymentThenOrder(orderId);
        return cancelLocked(locked.order(), locked.payment(), PaymentStatus.CANCELLED, reason, null, "ORDER_CANCELLED_BY_ADMIN");
    }

    public boolean expirePendingPayment(UUID paymentId, Instant now) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId).orElse(null);
        if (payment == null || payment.getStatus() != PaymentStatus.PENDING
                || payment.getExpiresAt() == null || payment.getExpiresAt().isAfter(now)) {
            return false;
        }
        Order order = orderRepository.findByIdForUpdate(payment.getOrderId()).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING || order.getPaymentStatus() != PaymentStatus.PENDING) {
            return false;
        }

        cancelLocked(
                order,
                payment,
                PaymentStatus.EXPIRED,
                "Đơn hàng đã hết hạn thanh toán QR",
                null,
                "ORDER_PAYMENT_EXPIRED"
        );
        return true;
    }

    private LockedOrderPayment lockPaymentThenOrder(UUID orderId) {
        Payment payment = paymentRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        return new LockedOrderPayment(payment, order);
    }

    private Order cancelLocked(
            Order order,
            Payment payment,
            PaymentStatus targetPaymentStatus,
            String reason,
            UUID actorId,
            String auditAction
    ) {
        if (order.getPaymentStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.PAID) {
            throw new ApplicationException(ApplicationErrorCode.ORDER_PAID_REFUND_REQUIRED);
        }
        if (order.getStatus() != OrderStatus.PENDING || order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
        }

        Instant now = Instant.now();
        if (targetPaymentStatus == PaymentStatus.EXPIRED) {
            payment.markExpired(now);
        } else {
            payment.markCancelled(now);
        }
        order.cancel(now);
        if (targetPaymentStatus == PaymentStatus.EXPIRED) {
            order.markPaymentExpired(now);
        } else {
            order.markPaymentCancelled(now);
        }
        rollbackStockAndCoupons(order, now);

        Payment savedPayment = paymentRepository.save(payment);
        Order savedOrder = orderRepository.save(order);
        digitalLibraryService.revokePurchasedAccessForOrder(savedOrder.getId());
        enqueueCancellationNotification(savedOrder, reason, targetPaymentStatus);
        orderTimelineService.recordOrderCancelled(savedOrder, reason);
        orderTimelineService.recordStockRolledBack(savedOrder);
        recordCouponRollbacks(savedOrder);
        auditLogService.recordStatusChange(new AuditLogCommand(
                actorId,
                null,
                actorId == null ? "SYSTEM" : "USER",
                auditAction,
                "ORDER",
                savedOrder.getId().toString(),
                "Đơn hàng " + savedOrder.getOrderCode() + " đã được hủy: " + reason,
                Map.of("orderStatus", OrderStatus.PENDING.name(), "paymentStatus", PaymentStatus.PENDING.name()),
                Map.of("orderStatus", savedOrder.getStatus().name(), "paymentStatus", savedPayment.getStatus().name()),
                null,
                null,
                now
        ));
        return savedOrder;
    }

    private void rollbackStockAndCoupons(Order order, Instant now) {
        Map<UUID, Book> booksById = loadPhysicalOrderBooks(order);
        for (OrderItem item : order.getItems().stream()
                .filter(current -> current.getItemType() == PurchaseItemType.PHYSICAL_BOOK)
                .toList()) {
            Book book = booksById.get(item.getBookId());
            int beforeQuantity = book.getStockQuantity();
            book.increaseStock(item.getQuantity());
            stockMovementRepository.save(new StockMovement(
                    UUID.randomUUID(), book.getId(), StockMovementType.CANCEL_ORDER, item.getQuantity(),
                    beforeQuantity, book.getStockQuantity(), order.getId(), "ORDER", null, now, order.getUserId()
            ));
        }
        booksById.values().forEach(bookRepository::save);

        if (order.getBookCouponId() != null) {
            rollbackCouponUsage(order.getBookCouponId(), now);
        }
        if (order.getShippingCouponId() != null && !Objects.equals(order.getShippingCouponId(), order.getBookCouponId())) {
            rollbackCouponUsage(order.getShippingCouponId(), now);
        }
        if (order.getBookCouponId() != null || order.getShippingCouponId() != null) {
            couponUsageRepository.deleteByOrderId(order.getId());
        }
    }

    private Map<UUID, Book> loadPhysicalOrderBooks(Order order) {
        List<UUID> bookIds = order.getItems().stream()
                .filter(item -> item.getItemType() == PurchaseItemType.PHYSICAL_BOOK)
                .map(OrderItem::getBookId)
                .toList();
        if (bookIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeletedForUpdate(bookIds).stream()
                .collect(LinkedHashMap::new, (map, book) -> map.put(book.getId(), book), Map::putAll);
        if (booksById.size() != bookIds.stream().distinct().count()) {
            throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
        }
        return booksById;
    }

    private void rollbackCouponUsage(UUID couponId, Instant rolledBackAt) {
        Coupon coupon = couponRepository.findByIdIncludingDeletedForUpdate(couponId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.COUPON_NOT_FOUND));
        coupon.rollbackUsage(rolledBackAt);
        couponRepository.save(coupon);
    }

    private void recordCouponRollbacks(Order order) {
        if (order.getBookCouponCode() != null) {
            orderTimelineService.recordCouponRolledBack(order, order.getBookCouponCode());
        }
        if (order.getShippingCouponCode() != null
                && !Objects.equals(order.getShippingCouponId(), order.getBookCouponId())) {
            orderTimelineService.recordCouponRolledBack(order, order.getShippingCouponCode());
        }
    }

    private CreateNotificationCommand newCancellationNotification(Order order, String reason) {
        return new CreateNotificationCommand(
                order.getUserId(),
                "Đơn hàng đã hủy",
                "Đơn hàng " + order.getOrderCode() + " đã được hủy. Lý do: " + reason,
                "ORDER", "ORDER", order.getId(), "/orders/" + order.getId()
        );
    }

    private void enqueueCancellationNotification(Order order, String reason, PaymentStatus targetPaymentStatus) {
        CreateNotificationCommand notification = newCancellationNotification(order, reason);
        String eventType = targetPaymentStatus == PaymentStatus.EXPIRED ? "PAYMENT_EXPIRED" : "ORDER_CANCELLED";
        transactionalOutboxService.enqueue(new com.bookstore.bookstore.application.command.EnqueueOutboxEventCommand(
                "ORDER", order.getId(), eventType,
                new com.bookstore.bookstore.application.command.OutboxNotificationPayload(
                        notification.userId(), notification.title(), notification.content(), notification.type(),
                        notification.targetType(), notification.targetId(), notification.link()
                ),
                order.getId() + "|notification|" + eventType
        ));
    }

    private record LockedOrderPayment(Payment payment, Order order) {
    }
}
