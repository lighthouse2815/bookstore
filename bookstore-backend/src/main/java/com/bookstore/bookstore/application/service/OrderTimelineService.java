package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.OrderTimelineEventAssembler;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IOrderTimelineEventRepository;
import com.bookstore.bookstore.application.result.OrderTimelineEventResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.OrderTimelineEvent;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.bookstore.bookstore.domain.model.Shipment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimelineService implements IOrderTimelineService {

    private static final String EVENT_ORDER_CREATED = "ORDER_CREATED";
    private static final String EVENT_COUPON_APPLIED = "COUPON_APPLIED";
    private static final String EVENT_PAYMENT_PENDING = "PAYMENT_PENDING";
    private static final String EVENT_PAYMENT_PAID = "PAYMENT_PAID";
    private static final String EVENT_ORDER_STATUS_CHANGED = "ORDER_STATUS_CHANGED";
    private static final String EVENT_SHIPMENT_ASSIGNED = "SHIPMENT_ASSIGNED";
    private static final String EVENT_SHIPMENT_STATUS_CHANGED = "SHIPMENT_STATUS_CHANGED";
    private static final String EVENT_ORDER_CANCELLED = "ORDER_CANCELLED";
    private static final String EVENT_STOCK_ROLLED_BACK = "STOCK_ROLLED_BACK";
    private static final String EVENT_COUPON_ROLLED_BACK = "COUPON_ROLLED_BACK";
    private static final String EVENT_RETURN_REQUESTED = "RETURN_REQUESTED";
    private static final String EVENT_RETURN_APPROVED = "RETURN_APPROVED";
    private static final String EVENT_RETURN_REJECTED = "RETURN_REJECTED";
    private static final String EVENT_RETURN_CANCELLED = "RETURN_CANCELLED";
    private static final String EVENT_REFUND_INTERNAL_APPROVED = "REFUND_INTERNAL_APPROVED";
    private static final String EVENT_STOCK_RESTOCKED_FROM_RETURN = "STOCK_RESTOCKED_FROM_RETURN";

    private final IOrderTimelineEventRepository orderTimelineEventRepository;
    private final IOrderRepository orderRepository;
    private final OrderTimelineEventAssembler orderTimelineEventAssembler;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderTimelineEventResult> getMyTimeline(UUID userId, UUID orderId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        Order order = loadOrder(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND);
        }
        return loadTimeline(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTimelineEventResult> getOrderTimeline(UUID orderId) {
        return loadTimeline(loadOrder(orderId));
    }

    @Override
    public void recordOrderCreated(Order order) {
        if (order == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("orderCode", order.getOrderCode());
        metadata.put("paymentMethod", order.getPaymentMethod().name());
        metadata.put("totalAmount", order.getTotalAmount());

        enqueue(newEvent(
                order,
                EVENT_ORDER_CREATED,
                "Đơn hàng đã được tạo",
                "Đơn hàng " + order.getOrderCode() + " đã được tạo thành công.",
                null,
                null,
                toMetadata(metadata),
                order.getCreatedAt()
        ));
    }

    @Override
    public void recordCouponsApplied(Order order) {
        if (order == null) {
            return;
        }

        List<String> couponCodes = new ArrayList<>();
        if (order.getBookCouponCode() != null) {
            couponCodes.add(order.getBookCouponCode());
        }
        if (order.getShippingCouponCode() != null
                && !Objects.equals(order.getShippingCouponCode(), order.getBookCouponCode())) {
            couponCodes.add(order.getShippingCouponCode());
        }
        if (couponCodes.isEmpty()) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("couponCodes", couponCodes);

        String description = couponCodes.size() == 1
                ? "Đã áp dụng mã giảm giá " + couponCodes.get(0) + " cho đơn hàng " + order.getOrderCode() + "."
                : "Đã áp dụng các mã giảm giá " + String.join(", ", couponCodes)
                + " cho đơn hàng " + order.getOrderCode() + ".";

        enqueue(newEvent(
                order,
                EVENT_COUPON_APPLIED,
                "Đã áp dụng mã giảm giá",
                description,
                null,
                null,
                toMetadata(metadata),
                offset(order.getCreatedAt(), 1)
        ));
    }

    @Override
    public void recordPaymentPending(Order order, Payment payment) {
        if (order == null || payment == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("paymentMethod", order.getPaymentMethod().name());
        metadata.put("paymentProvider", payment.getProvider().name());
        metadata.put("referenceCode", payment.getReferenceCode());

        String description = order.getPaymentMethod() == PaymentMethod.COD
                ? "Đơn hàng " + order.getOrderCode() + " sẽ thanh toán khi giao thành công."
                : "Đơn hàng " + order.getOrderCode() + " đang chờ thanh toán qua " + paymentMethodLabel(order.getPaymentMethod()) + ".";

        enqueue(newEvent(
                order,
                EVENT_PAYMENT_PENDING,
                "Chờ thanh toán",
                description,
                null,
                null,
                toMetadata(metadata),
                offset(payment.getCreatedAt(), 2)
        ));
    }

    @Override
    public void recordPaymentPaid(Order order, Payment payment) {
        if (order == null || payment == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("paymentMethod", order.getPaymentMethod().name());
        metadata.put("paymentProvider", payment.getProvider().name());
        metadata.put("referenceCode", payment.getReferenceCode());
        metadata.put("transactionId", payment.getTransactionId());

        enqueue(newEvent(
                order,
                EVENT_PAYMENT_PAID,
                "Thanh toán thành công",
                "Thanh toán cho đơn hàng " + order.getOrderCode() + " đã được xác nhận thành công.",
                null,
                null,
                toMetadata(metadata),
                resolvePaymentPaidAt(payment, order)
        ));
    }

    @Override
    public void recordStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (order == null || oldStatus == null || newStatus == null || oldStatus == newStatus) {
            return;
        }

        enqueue(newEvent(
                order,
                EVENT_ORDER_STATUS_CHANGED,
                "Cập nhật trạng thái đơn hàng",
                "Trạng thái đơn hàng chuyển từ " + orderStatusLabel(oldStatus)
                        + " sang " + orderStatusLabel(newStatus) + ".",
                oldStatus.name(),
                newStatus.name(),
                null,
                resolveOrderStatusEventTime(order)
        ));
    }

    @Override
    public void recordShipmentAssigned(Order order, Shipment shipment, String shipperName) {
        if (order == null || shipment == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("shipmentId", shipment.getId());
        metadata.put("shipperId", shipment.getShipperId());
        if (shipperName != null && !shipperName.isBlank()) {
            metadata.put("shipperName", shipperName);
        }

        String description = shipperName == null || shipperName.isBlank()
                ? "Đơn hàng " + order.getOrderCode() + " đã được phân công giao hàng."
                : "Đơn hàng " + order.getOrderCode() + " đã được phân công cho shipper " + shipperName + ".";

        enqueue(newEvent(
                order,
                EVENT_SHIPMENT_ASSIGNED,
                "Đã phân công giao hàng",
                description,
                null,
                ShipmentStatus.ASSIGNED.name(),
                toMetadata(metadata),
                shipment.getAssignedAt()
        ));
    }

    @Override
    public void recordShipmentStatusChanged(
            Order order,
            Shipment shipment,
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus
    ) {
        if (order == null || shipment == null || oldStatus == null || newStatus == null || oldStatus == newStatus) {
            return;
        }

        String description = "Trạng thái giao hàng chuyển từ " + shipmentStatusLabel(oldStatus)
                + " sang " + shipmentStatusLabel(newStatus) + ".";
        if (newStatus == ShipmentStatus.FAILED && shipment.getFailureReason() != null) {
            description += " Lý do: " + shipment.getFailureReason() + ".";
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("shipmentId", shipment.getId());
        metadata.put("shipperId", shipment.getShipperId());
        if (shipment.getFailureReason() != null) {
            metadata.put("failureReason", shipment.getFailureReason());
        }

        enqueue(newEvent(
                order,
                EVENT_SHIPMENT_STATUS_CHANGED,
                "Cập nhật trạng thái giao hàng",
                description,
                oldStatus.name(),
                newStatus.name(),
                toMetadata(metadata),
                resolveShipmentEventTime(shipment, newStatus)
        ));
    }

    @Override
    public void recordOrderCancelled(Order order, String reason) {
        if (order == null) {
            return;
        }

        String description = "Đơn hàng " + order.getOrderCode() + " đã bị hủy.";
        if (reason != null && !reason.isBlank()) {
            description += " Lý do: " + reason.trim() + ".";
        }

        enqueue(newEvent(
                order,
                EVENT_ORDER_CANCELLED,
                "Đơn hàng đã bị hủy",
                description,
                null,
                OrderStatus.CANCELLED.name(),
                null,
                resolveCancelledAt(order)
        ));
    }

    @Override
    public void recordStockRolledBack(Order order) {
        if (order == null) {
            return;
        }

        long physicalItemCount = order.getItems().stream()
                .filter(item -> item.getItemType() == PurchaseItemType.PHYSICAL_BOOK)
                .count();
        int totalQuantity = order.getItems().stream()
                .filter(item -> item.getItemType() == PurchaseItemType.PHYSICAL_BOOK)
                .mapToInt(OrderItem::getQuantity)
                .sum();
        if (physicalItemCount == 0L) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("physicalItemCount", physicalItemCount);
        metadata.put("totalQuantity", totalQuantity);

        enqueue(newEvent(
                order,
                EVENT_STOCK_ROLLED_BACK,
                "Đã hoàn kho sản phẩm",
                "Đã hoàn lại tồn kho cho " + totalQuantity + " sản phẩm vật lý trong đơn hàng " + order.getOrderCode() + ".",
                null,
                null,
                toMetadata(metadata),
                offset(resolveCancelledAt(order), 1)
        ));
    }

    @Override
    public void recordCouponRolledBack(Order order, String couponCode) {
        if (order == null || couponCode == null || couponCode.isBlank()) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("couponCode", couponCode);

        long offsetMillis = Objects.equals(couponCode, order.getBookCouponCode()) ? 2L : 3L;

        enqueue(newEvent(
                order,
                EVENT_COUPON_ROLLED_BACK,
                "Đã hoàn lại mã giảm giá",
                "Đã hoàn lại lượt sử dụng cho mã giảm giá " + couponCode + ".",
                null,
                null,
                toMetadata(metadata),
                offset(resolveCancelledAt(order), offsetMillis)
        ));
    }

    @Override
    public void recordReturnRequested(Order order, ReturnRequest returnRequest) {
        if (order == null || returnRequest == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("reason", returnRequest.getReason());
        metadata.put("requestedRefundAmount", returnRequest.getRequestedRefundAmount());

        enqueue(newEvent(
                order,
                EVENT_RETURN_REQUESTED,
                "Đã tạo yêu cầu trả hàng",
                "Khách hàng đã tạo yêu cầu trả hàng cho đơn " + order.getOrderCode() + ".",
                null,
                returnRequest.getStatus().name(),
                toMetadata(metadata),
                returnRequest.getCreatedAt()
        ));
    }

    @Override
    public void recordReturnApproved(Order order, ReturnRequest returnRequest) {
        if (order == null || returnRequest == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("approvedRefundAmount", returnRequest.getApprovedRefundAmount());
        metadata.put("adminNote", returnRequest.getAdminNote());

        enqueue(newEvent(
                order,
                EVENT_RETURN_APPROVED,
                "Đã duyệt yêu cầu trả hàng",
                "Yêu cầu trả hàng cho đơn " + order.getOrderCode() + " đã được duyệt.",
                "PENDING",
                "APPROVED",
                toMetadata(metadata),
                resolveReturnRequestEventTime(returnRequest)
        ));
    }

    @Override
    public void recordReturnRejected(Order order, ReturnRequest returnRequest) {
        if (order == null || returnRequest == null) {
            return;
        }

        String description = "Yêu cầu trả hàng cho đơn " + order.getOrderCode() + " đã bị từ chối.";
        if (returnRequest.getAdminNote() != null && !returnRequest.getAdminNote().isBlank()) {
            description += " Lý do: " + returnRequest.getAdminNote() + ".";
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("adminNote", returnRequest.getAdminNote());

        enqueue(newEvent(
                order,
                EVENT_RETURN_REJECTED,
                "Đã từ chối yêu cầu trả hàng",
                description,
                "PENDING",
                "REJECTED",
                toMetadata(metadata),
                resolveReturnRequestEventTime(returnRequest)
        ));
    }

    @Override
    public void recordReturnCancelled(Order order, ReturnRequest returnRequest) {
        if (order == null || returnRequest == null) {
            return;
        }

        enqueue(newEvent(
                order,
                EVENT_RETURN_CANCELLED,
                "Đã hủy yêu cầu trả hàng",
                "Khách hàng đã hủy yêu cầu trả hàng của đơn " + order.getOrderCode() + ".",
                "PENDING",
                "CANCELLED",
                null,
                resolveReturnRequestEventTime(returnRequest)
        ));
    }

    @Override
    public void recordRefundInternalApproved(Order order, ReturnRequest returnRequest) {
        if (order == null || returnRequest == null) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("paymentMethod", order.getPaymentMethod().name());
        metadata.put("approvedRefundAmount", returnRequest.getApprovedRefundAmount());

        enqueue(newEvent(
                order,
                EVENT_REFUND_INTERNAL_APPROVED,
                "Đã duyệt hoàn tiền nội bộ",
                "Hoàn tiền nội bộ cho đơn " + order.getOrderCode() + " đã được ghi nhận theo phương thức "
                        + paymentMethodLabel(order.getPaymentMethod()) + ".",
                null,
                null,
                toMetadata(metadata),
                offset(resolveReturnRequestEventTime(returnRequest), 1)
        ));
    }

    @Override
    public void recordStockRestockedFromReturn(Order order, ReturnRequest returnRequest, int totalQuantity) {
        if (order == null || returnRequest == null || totalQuantity <= 0) {
            return;
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("totalQuantity", totalQuantity);

        enqueue(newEvent(
                order,
                EVENT_STOCK_RESTOCKED_FROM_RETURN,
                "Đã hoàn kho từ trả hàng",
                "Đã hoàn lại tồn kho cho " + totalQuantity + " sản phẩm vật lý từ yêu cầu trả hàng của đơn "
                        + order.getOrderCode() + ".",
                null,
                null,
                toMetadata(metadata),
                offset(resolveReturnRequestEventTime(returnRequest), 2)
        ));
    }

    private List<OrderTimelineEventResult> loadTimeline(Order order) {
        List<OrderTimelineEvent> persistedEvents = orderTimelineEventRepository.findByOrderId(order.getId());
        List<OrderTimelineEvent> events = persistedEvents.isEmpty()
                ? deriveSyntheticTimeline(order)
                : persistedEvents;

        return events.stream()
                .sorted(Comparator.comparing(OrderTimelineEvent::getCreatedAt)
                        .thenComparing(event -> event.getId().toString()))
                .map(orderTimelineEventAssembler::toResult)
                .toList();
    }

    private Order loadOrder(UUID orderId) {
        if (orderId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "orderId");
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
    }

    private void enqueue(OrderTimelineEvent event) {
        if (event == null) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    persistQuietly(event);
                }
            });
            return;
        }

        persistQuietly(event);
    }

    private void persistQuietly(OrderTimelineEvent event) {
        try {
            orderTimelineEventRepository.save(event);
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to persist order timeline event orderId={} eventType={}",
                    event.getOrderId(),
                    event.getEventType(),
                    exception
            );
        }
    }

    private OrderTimelineEvent newEvent(
            Order order,
            String eventType,
            String title,
            String description,
            String oldStatus,
            String newStatus,
            String metadata,
            Instant createdAt
    ) {
        TimelineActor actor = resolveCurrentActor();
        return new OrderTimelineEvent(
                UUID.randomUUID(),
                order.getId(),
                actor.actorId(),
                actor.actorName(),
                actor.actorRole(),
                eventType,
                oldStatus,
                newStatus,
                title,
                description,
                metadata,
                createdAt == null ? Instant.now() : createdAt
        );
    }

    private TimelineActor resolveCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return new TimelineActor(null, null, null);
        }

        UUID actorId = null;
        if (jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            try {
                actorId = UUID.fromString(jwt.getSubject());
            } catch (IllegalArgumentException ignored) {
                actorId = null;
            }
        }

        return new TimelineActor(
                actorId,
                jwt.getClaimAsString("username"),
                resolveActorRole(jwt.getClaimAsStringList("roles"))
        );
    }

    private String resolveActorRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        if (roles.contains("ADMIN")) {
            return "ADMIN";
        }
        if (roles.contains("STAFF")) {
            return "STAFF";
        }
        if (roles.contains("SHIPPER")) {
            return "SHIPPER";
        }
        if (roles.contains("USER")) {
            return "USER";
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private List<OrderTimelineEvent> deriveSyntheticTimeline(Order order) {
        List<OrderTimelineEvent> events = new ArrayList<>();
        Instant createdAt = order.getCreatedAt();

        events.add(syntheticEvent(
                order,
                EVENT_ORDER_CREATED,
                "Đơn hàng đã được tạo",
                "Đơn hàng " + order.getOrderCode() + " đã được tạo thành công.",
                null,
                null,
                createdAt,
                "created"
        ));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            events.add(syntheticEvent(
                    order,
                    EVENT_PAYMENT_PAID,
                    "Thanh toán thành công",
                    "Thanh toán cho đơn hàng " + order.getOrderCode() + " đã được xác nhận thành công.",
                    null,
                    null,
                    resolveSyntheticPaymentTime(order),
                    "payment-paid"
            ));
        } else {
            events.add(syntheticEvent(
                    order,
                    EVENT_PAYMENT_PENDING,
                    "Chờ thanh toán",
                    "Đơn hàng " + order.getOrderCode() + " đang ở trạng thái chờ thanh toán.",
                    null,
                    null,
                    offset(createdAt, 1),
                    "payment-pending"
            ));
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            events.add(syntheticEvent(
                    order,
                    EVENT_ORDER_CANCELLED,
                    "Đơn hàng đã bị hủy",
                    "Đơn hàng " + order.getOrderCode() + " đã bị hủy.",
                    null,
                    OrderStatus.CANCELLED.name(),
                    resolveCancelledAt(order),
                    "cancelled"
            ));
        } else if (order.getStatus() != OrderStatus.PENDING) {
            events.add(syntheticEvent(
                    order,
                    EVENT_ORDER_STATUS_CHANGED,
                    "Cập nhật trạng thái đơn hàng",
                    "Đơn hàng hiện ở trạng thái " + orderStatusLabel(order.getStatus()) + ".",
                    null,
                    order.getStatus().name(),
                    offset(resolveOrderStatusEventTime(order), 1),
                    "status"
            ));
        }

        return events;
    }

    private OrderTimelineEvent syntheticEvent(
            Order order,
            String eventType,
            String title,
            String description,
            String oldStatus,
            String newStatus,
            Instant createdAt,
            String suffix
    ) {
        return new OrderTimelineEvent(
                deterministicId(order.getId(), suffix),
                order.getId(),
                null,
                null,
                null,
                eventType,
                oldStatus,
                newStatus,
                title,
                description,
                null,
                createdAt
        );
    }

    private UUID deterministicId(UUID orderId, String suffix) {
        return UUID.nameUUIDFromBytes((orderId + ":" + suffix).getBytes(StandardCharsets.UTF_8));
    }

    private String toMetadata(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize order timeline metadata", exception);
            return null;
        }
    }

    private Instant resolvePaymentPaidAt(Payment payment, Order order) {
        if (payment.getPaidAt() != null) {
            return payment.getPaidAt();
        }
        if (order.getUpdatedAt() != null) {
            return order.getUpdatedAt();
        }
        return Instant.now();
    }

    private Instant resolveOrderStatusEventTime(Order order) {
        if (order.getUpdatedAt() != null) {
            return order.getUpdatedAt();
        }
        return Instant.now();
    }

    private Instant resolveShipmentEventTime(Shipment shipment, ShipmentStatus status) {
        return switch (status) {
            case ASSIGNED -> shipment.getAssignedAt();
            case PICKED_UP -> shipment.getPickedUpAt() != null ? shipment.getPickedUpAt() : shipment.getUpdatedAt();
            case DELIVERING -> shipment.getDeliveringAt() != null ? shipment.getDeliveringAt() : shipment.getUpdatedAt();
            case DELIVERED -> shipment.getDeliveredAt() != null ? shipment.getDeliveredAt() : shipment.getUpdatedAt();
            case FAILED -> shipment.getFailedAt() != null ? shipment.getFailedAt() : shipment.getUpdatedAt();
        };
    }

    private Instant resolveCancelledAt(Order order) {
        if (order.getCancelledAt() != null) {
            return order.getCancelledAt();
        }
        if (order.getUpdatedAt() != null) {
            return order.getUpdatedAt();
        }
        return Instant.now();
    }

    private Instant resolveSyntheticPaymentTime(Order order) {
        if (order.getUpdatedAt() != null && order.getUpdatedAt().isAfter(order.getCreatedAt())) {
            return order.getUpdatedAt();
        }
        return offset(order.getCreatedAt(), 1);
    }

    private Instant resolveReturnRequestEventTime(ReturnRequest returnRequest) {
        if (returnRequest.getProcessedAt() != null) {
            return returnRequest.getProcessedAt();
        }
        if (returnRequest.getUpdatedAt() != null) {
            return returnRequest.getUpdatedAt();
        }
        if (returnRequest.getCreatedAt() != null) {
            return returnRequest.getCreatedAt();
        }
        return Instant.now();
    }

    private Instant offset(Instant instant, long millis) {
        Instant base = instant == null ? Instant.now() : instant;
        return base.plusMillis(millis);
    }

    private String orderStatusLabel(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao";
            case DELIVERED -> "Đã giao";
            case CANCELLED -> "Đã hủy";
        };
    }

    private String shipmentStatusLabel(ShipmentStatus status) {
        return switch (status) {
            case ASSIGNED -> "Đã phân công";
            case PICKED_UP -> "Đã lấy hàng";
            case DELIVERING -> "Đang giao";
            case DELIVERED -> "Đã giao";
            case FAILED -> "Giao thất bại";
        };
    }

    private String paymentMethodLabel(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case CASH -> "tiền mặt";
            case BANK_TRANSFER -> "chuyển khoản";
            case BANK_TRANSFER_QR -> "chuyển khoản QR";
            case COD -> "thanh toán khi nhận hàng";
        };
    }

    private record TimelineActor(UUID actorId, String actorName, String actorRole) {
    }
}
