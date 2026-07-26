package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.AssignShipmentCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.assembler.ShipmentAssembler;
import com.bookstore.bookstore.application.command.UpdateShipmentStatusCommand;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.application.port.out.IShipmentRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.ShipmentResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.Shipment;
import com.bookstore.bookstore.domain.model.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private IShipmentRepository shipmentRepository;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private INotificationService notificationService;

    @Mock
    private ShipmentAssembler shipmentAssembler;

    @Mock
    private IDigitalLibraryService digitalLibraryService;

    @Mock
    private IOrderTimelineService orderTimelineService;

    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    void assign_whenOrderAlreadyHasActiveShipment_rejectsDuplicateAssignment() {
        UUID shipperId = UUID.randomUUID();
        Order order = order();
        Shipment activeShipment = shipment(order.getId(), shipperId, ShipmentStatus.ASSIGNED);

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(shipmentRepository.findAllByOrderId(order.getId())).thenReturn(List.of(activeShipment));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> shipmentService.assign(new AssignShipmentCommand(order.getId(), shipperId))
        );

        assertEquals(ApplicationErrorCode.SHIPMENT_ORDER_ALREADY_HAS_ACTIVE_ASSIGNMENT, exception.getErrorCode());
        verify(orderRepository, never()).save(any(Order.class));
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void assign_whenConfirmedOrder_recordsTimelineAndStartsShipping() {
        UUID shipperId = UUID.randomUUID();
        Order order = confirmedOrder();
        User shipper = shipperUser(shipperId);
        ShipmentResult expected = new ShipmentResult(
                UUID.randomUUID(),
                order.getId(),
                order.getOrderCode(),
                shipperId,
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                OrderStatus.SHIPPING,
                ShipmentStatus.ASSIGNED,
                order.getTotalAmount(),
                order.getFinalAmount(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                null,
                order.getUpdatedAt(),
                order.getUpdatedAt(),
                null,
                null,
                null,
                null
        );

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(shipmentRepository.findAllByOrderId(order.getId())).thenReturn(List.of());
        when(userRepository.findByIdActive(shipperId)).thenReturn(Optional.of(shipper));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentAssembler.toResult(any(Shipment.class), any(Order.class))).thenReturn(expected);

        ShipmentResult result = shipmentService.assign(new AssignShipmentCommand(order.getId(), shipperId));

        verify(orderTimelineService).recordStatusChanged(order, OrderStatus.CONFIRMED, OrderStatus.SHIPPING);
        verify(orderTimelineService).recordShipmentAssigned(
                org.mockito.ArgumentMatchers.eq(order),
                org.mockito.ArgumentMatchers.any(Shipment.class),
                org.mockito.ArgumentMatchers.eq("shipper.test")
        );
        assertEquals(OrderStatus.SHIPPING, order.getStatus());
        assertEquals(expected, result);
    }

    @Test
    void updateMyShipmentStatus_whenShipmentBelongsToAnotherShipper_rejectsNotFound() {
        UUID shipperId = UUID.randomUUID();
        UUID shipmentId = UUID.randomUUID();

        when(shipmentRepository.findByIdAndShipperId(shipmentId, shipperId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> shipmentService.updateMyShipmentStatus(
                        new UpdateShipmentStatusCommand(shipmentId, shipperId, ShipmentStatus.PICKED_UP, null)
                )
        );

        assertEquals(ApplicationErrorCode.SHIPMENT_NOT_FOUND, exception.getErrorCode());
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void updateMyShipmentStatus_whenSkippingTransition_rejectsInvalidShipmentTransition() {
        UUID shipperId = UUID.randomUUID();
        Order order = order();
        Shipment shipment = shipment(order.getId(), shipperId, ShipmentStatus.ASSIGNED);

        when(shipmentRepository.findByIdAndShipperId(shipment.getId(), shipperId)).thenReturn(Optional.of(shipment));
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> shipmentService.updateMyShipmentStatus(
                        new UpdateShipmentStatusCommand(shipment.getId(), shipperId, ShipmentStatus.DELIVERED, null)
                )
        );

        assertEquals(DomainErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION, exception.getErrorCode());
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void updateMyShipmentStatus_whenCodOrderDelivered_grantsDigitalLibraryAccess() {
        UUID shipperId = UUID.randomUUID();
        Order order = order();
        Shipment shipment = shipment(order.getId(), shipperId, ShipmentStatus.DELIVERING);
        ShipmentResult expected = new ShipmentResult(
                shipment.getId(),
                order.getId(),
                order.getOrderCode(),
                shipperId,
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                OrderStatus.DELIVERED,
                ShipmentStatus.DELIVERED,
                order.getTotalAmount(),
                order.getFinalAmount(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                null,
                shipment.getAssignedAt(),
                shipment.getUpdatedAt(),
                shipment.getPickedUpAt(),
                shipment.getDeliveringAt(),
                shipment.getDeliveredAt(),
                shipment.getFailedAt()
        );

        when(shipmentRepository.findByIdAndShipperId(shipment.getId(), shipperId)).thenReturn(Optional.of(shipment));
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentAssembler.toResult(any(Shipment.class), any(Order.class))).thenReturn(expected);

        ShipmentResult result = shipmentService.updateMyShipmentStatus(
                new UpdateShipmentStatusCommand(shipment.getId(), shipperId, ShipmentStatus.DELIVERED, null)
        );

        verify(digitalLibraryService).grantPurchasedAccessForOrder(order);
        verify(notificationService).create(any());
        verify(orderTimelineService).recordShipmentStatusChanged(
                order,
                shipment,
                ShipmentStatus.DELIVERING,
                ShipmentStatus.DELIVERED
        );
        verify(orderTimelineService).recordStatusChanged(order, OrderStatus.SHIPPING, OrderStatus.DELIVERED);
        assertEquals(expected, result);
    }

    @Test
    void confirmDeliveredByAdmin_whenCodShipmentDelivered_marksOrderAndPaymentPaid() {
        UUID shipperId = UUID.randomUUID();
        Order order = order();
        Shipment shipment = shipment(order.getId(), shipperId, ShipmentStatus.DELIVERING);
        Payment payment = payment(order.getId());
        ShipmentResult expected = new ShipmentResult(
                shipment.getId(),
                order.getId(),
                order.getOrderCode(),
                shipperId,
                order.getPaymentMethod(),
                PaymentStatus.PAID,
                OrderStatus.DELIVERED,
                ShipmentStatus.DELIVERED,
                order.getTotalAmount(),
                order.getFinalAmount(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                null,
                shipment.getAssignedAt(),
                shipment.getUpdatedAt(),
                shipment.getPickedUpAt(),
                shipment.getDeliveringAt(),
                shipment.getDeliveredAt(),
                shipment.getFailedAt()
        );

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(payment));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentAssembler.toResult(any(Shipment.class), any(Order.class))).thenReturn(expected);

        ShipmentResult result = shipmentService.confirmDeliveredByAdmin(shipment.getId());

        verify(paymentRepository).save(payment);
        verify(digitalLibraryService).grantPurchasedAccessForOrder(order);
        verify(orderTimelineService).recordShipmentStatusChanged(
                order,
                shipment,
                ShipmentStatus.DELIVERING,
                ShipmentStatus.DELIVERED
        );
        verify(orderTimelineService).recordStatusChanged(order, OrderStatus.SHIPPING, OrderStatus.DELIVERED);
        verify(orderTimelineService).recordPaymentPaid(order, payment);
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(payment.getPaidAt(), payment.getUpdatedAt());
        assertEquals(expected, result);
    }

    @Test
    void confirmDeliveredByAdmin_whenOnlinePaymentPending_rejectsConflict() {
        UUID shipperId = UUID.randomUUID();
        Order order = onlineOrder(PaymentStatus.PENDING);
        Shipment shipment = shipment(order.getId(), shipperId, ShipmentStatus.DELIVERING);

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> shipmentService.confirmDeliveredByAdmin(shipment.getId())
        );

        assertEquals(ApplicationErrorCode.ORDER_PAYMENT_NOT_PAID, exception.getErrorCode());
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    private static Order order() {
        Instant now = Instant.EPOCH;
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Book Title",
                new BigDecimal("10.00"),
                1,
                new BigDecimal("10.00")
        );
        return new Order(
                UUID.randomUUID(),
                "DH123",
                UUID.randomUUID(),
                List.of(item),
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                null,
                null,
                null,
                null,
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                OrderStatus.SHIPPING,
                "Receiver Name",
                "0123456789",
                "Receiver Address",
                now,
                now,
                null
        );
    }

    private static Order confirmedOrder() {
        Instant now = Instant.EPOCH;
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Book Title",
                new BigDecimal("10.00"),
                1,
                new BigDecimal("10.00")
        );
        return new Order(
                UUID.randomUUID(),
                "DH123",
                UUID.randomUUID(),
                List.of(item),
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                null,
                null,
                null,
                null,
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                OrderStatus.CONFIRMED,
                "Receiver Name",
                "0123456789",
                "Receiver Address",
                now,
                now,
                null
        );
    }

    private static Order onlineOrder(PaymentStatus paymentStatus) {
        Instant now = Instant.EPOCH;
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Book Title",
                new BigDecimal("10.00"),
                1,
                new BigDecimal("10.00")
        );
        return new Order(
                UUID.randomUUID(),
                "DH999",
                UUID.randomUUID(),
                List.of(item),
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                null,
                null,
                null,
                null,
                PaymentMethod.BANK_TRANSFER_QR,
                paymentStatus,
                OrderStatus.SHIPPING,
                "Receiver Name",
                "0123456789",
                "Receiver Address",
                now,
                now,
                null
        );
    }

    private static Shipment shipment(UUID orderId, UUID shipperId, ShipmentStatus status) {
        Instant now = Instant.EPOCH;
        Instant pickedUpAt = status == ShipmentStatus.ASSIGNED ? null : now;
        Instant deliveringAt = (status == ShipmentStatus.DELIVERING || status == ShipmentStatus.DELIVERED || status == ShipmentStatus.FAILED)
                ? now
                : null;
        Instant deliveredAt = status == ShipmentStatus.DELIVERED ? now : null;
        Instant failedAt = status == ShipmentStatus.FAILED ? now : null;

        return new Shipment(
                UUID.randomUUID(),
                orderId,
                shipperId,
                status,
                status == ShipmentStatus.FAILED ? "Giao thất bại" : null,
                now,
                now,
                pickedUpAt,
                deliveringAt,
                deliveredAt,
                failedAt
        );
    }

    private static Payment payment(UUID orderId) {
        Instant now = Instant.EPOCH;
        return new Payment(
                UUID.randomUUID(),
                orderId,
                PaymentProvider.COD,
                PaymentStatus.PENDING,
                new BigDecimal("10.00"),
                null,
                null,
                "DH123",
                "DH123",
                null,
                null,
                now,
                now
        );
    }

    private static User shipperUser(UUID shipperId) {
        Instant now = Instant.EPOCH;
        Role shipperRole = new Role(
                UUID.randomUUID(),
                "SHIPPER",
                "Shipper",
                Set.of(),
                now,
                now,
                null
        );
        return new User(
                shipperId,
                "shipper.test",
                "hashed-password",
                "0123456789",
                "shipper@example.com",
                UserStatus.ACTIVE,
                false,
                Set.of(shipperRole),
                now,
                now,
                null
        );
    }
}
