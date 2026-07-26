package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.OrderTimelineEventAssembler;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IOrderTimelineEventRepository;
import com.bookstore.bookstore.application.result.OrderTimelineEventResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.OrderTimelineEvent;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderTimelineServiceTest {

    private static final UUID ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000111");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000222");
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000333");

    @Mock
    private IOrderTimelineEventRepository orderTimelineEventRepository;

    @Mock
    private IOrderRepository orderRepository;

    private OrderTimelineService orderTimelineService;

    @BeforeEach
    void setUp() {
        orderTimelineService = new OrderTimelineService(
                orderTimelineEventRepository,
                orderRepository,
                new OrderTimelineEventAssembler(),
                new ObjectMapper()
        );
    }

    @Test
    void recordOrderCreated_persistsOrderCreatedEvent() {
        Order order = order(OWNER_ID, OrderStatus.PENDING, PaymentStatus.PENDING, Instant.EPOCH, Instant.EPOCH, null);
        when(orderTimelineEventRepository.save(org.mockito.ArgumentMatchers.any(OrderTimelineEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderTimelineService.recordOrderCreated(order);

        verify(orderTimelineEventRepository).save(org.mockito.ArgumentMatchers.argThat(event ->
                event.getOrderId().equals(order.getId())
                        && event.getEventType().equals("ORDER_CREATED")
                        && event.getTitle().equals("Đơn hàng đã được tạo")
        ));
    }

    @Test
    void getMyTimeline_whenOrderBelongsToDifferentUser_throwsOrderNotFound() {
        when(orderRepository.findById(ORDER_ID))
                .thenReturn(Optional.of(order(OWNER_ID, OrderStatus.PENDING, PaymentStatus.PENDING, Instant.EPOCH, Instant.EPOCH, null)));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> orderTimelineService.getMyTimeline(OTHER_USER_ID, ORDER_ID)
        );

        assertEquals(ApplicationErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getOrderTimeline_whenPersistedEventsExist_returnsSortedResults() {
        Order order = order(OWNER_ID, OrderStatus.SHIPPING, PaymentStatus.PAID, Instant.EPOCH, Instant.EPOCH.plusSeconds(30), null);
        OrderTimelineEvent laterEvent = timelineEvent(
                ORDER_ID,
                "SHIPMENT_ASSIGNED",
                "Đã phân công giao hàng",
                Instant.EPOCH.plusSeconds(20)
        );
        OrderTimelineEvent earlierEvent = timelineEvent(
                ORDER_ID,
                "ORDER_CREATED",
                "Đơn hàng đã được tạo",
                Instant.EPOCH
        );

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderTimelineEventRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(laterEvent, earlierEvent));

        List<OrderTimelineEventResult> results = orderTimelineService.getOrderTimeline(ORDER_ID);

        assertEquals(2, results.size());
        assertEquals("ORDER_CREATED", results.get(0).eventType());
        assertEquals("SHIPMENT_ASSIGNED", results.get(1).eventType());
    }

    @Test
    void getOrderTimeline_whenNoPersistedEvents_returnsSyntheticFallback() {
        Order order = order(
                OWNER_ID,
                OrderStatus.DELIVERED,
                PaymentStatus.PAID,
                Instant.parse("2026-07-08T10:00:00Z"),
                Instant.parse("2026-07-08T10:30:00Z"),
                null
        );

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderTimelineEventRepository.findByOrderId(ORDER_ID)).thenReturn(List.of());

        List<OrderTimelineEventResult> results = orderTimelineService.getOrderTimeline(ORDER_ID);

        assertEquals(3, results.size());
        assertEquals("ORDER_CREATED", results.get(0).eventType());
        assertEquals("PAYMENT_PAID", results.get(1).eventType());
        assertEquals("ORDER_STATUS_CHANGED", results.get(2).eventType());
    }

    @Test
    void recordReturnRequested_persistsReturnRequestedEvent() {
        Order order = order(OWNER_ID, OrderStatus.DELIVERED, PaymentStatus.PAID, Instant.EPOCH, Instant.EPOCH, null);
        ReturnRequest returnRequest = new ReturnRequest(
                UUID.randomUUID(),
                order.getId(),
                OWNER_ID,
                "Bị lỗi",
                ReturnRequestStatus.PENDING,
                null,
                new BigDecimal("50000"),
                null,
                null,
                null,
                Instant.EPOCH.plusSeconds(5),
                Instant.EPOCH.plusSeconds(5),
                null
        );
        when(orderTimelineEventRepository.save(org.mockito.ArgumentMatchers.any(OrderTimelineEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderTimelineService.recordReturnRequested(order, returnRequest);

        verify(orderTimelineEventRepository).save(org.mockito.ArgumentMatchers.argThat(event ->
                event.getOrderId().equals(order.getId())
                        && event.getEventType().equals("RETURN_REQUESTED")
                        && event.getTitle().equals("Đã tạo yêu cầu trả hàng")
        ));
    }

    private static Order order(
            UUID userId,
            OrderStatus status,
            PaymentStatus paymentStatus,
            Instant createdAt,
            Instant updatedAt,
            Instant cancelledAt
    ) {
        return new Order(
                ORDER_ID,
                "DH-TEST-001",
                userId,
                List.of(new OrderItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Book Title",
                        new BigDecimal("100000"),
                        1,
                        new BigDecimal("100000")
                )),
                new BigDecimal("100000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100000"),
                null,
                null,
                null,
                null,
                PaymentMethod.COD,
                paymentStatus,
                status,
                "Nguyen Van A",
                "0900000000",
                "123 Test Street",
                createdAt,
                updatedAt,
                cancelledAt
        );
    }

    private static OrderTimelineEvent timelineEvent(
            UUID orderId,
            String eventType,
            String title,
            Instant createdAt
    ) {
        return new OrderTimelineEvent(
                UUID.randomUUID(),
                orderId,
                UUID.randomUUID(),
                "admin",
                "ADMIN",
                eventType,
                null,
                null,
                title,
                title,
                null,
                createdAt
        );
    }
}
