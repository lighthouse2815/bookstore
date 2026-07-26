package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.HandleSepayIpnCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.in.IPaymentReconciliationService;
import com.bookstore.bookstore.application.port.in.ITransactionalOutboxService;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.infrastructure.payment.SepayProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IDigitalLibraryService digitalLibraryService;

    @Mock
    private IOrderTimelineService orderTimelineService;

    @Mock
    private IPaymentReconciliationService paymentReconciliationService;

    @Mock
    private ITransactionalOutboxService transactionalOutboxService;

    @InjectMocks
    private PaymentService paymentService;

    PaymentServiceTest() {
    }

    @Test
    void handleSepayIpn_whenPaymentIsMatched_marksPaidAndGrantsLibraryAccess() {
        paymentService = new PaymentService(
                paymentRepository,
                orderRepository,
                digitalLibraryService,
                orderTimelineService,
                new SepayProperties("merchant-123", null, "webhook-key"),
                paymentReconciliationService,
                transactionalOutboxService
        );

        Payment payment = payment();
        Order order = order(payment.getOrderId());
        HandleSepayIpnCommand command = new HandleSepayIpnCommand(
                "Apikey webhook-key",
                null,
                "TXN-001",
                "SEPAY",
                null,
                null,
                null,
                "DH123",
                "DH123",
                "in",
                null,
                new BigDecimal("10.00"),
                "REF-001",
                null
        );

        when(paymentRepository.findByTransactionId("TXN-001")).thenReturn(Optional.empty());
        when(paymentRepository.findByReferenceCode("REF-001")).thenReturn(Optional.empty());
        when(paymentRepository.findSepayByOrderCodeForUpdate("DH123")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdForUpdate(payment.getOrderId())).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.handleSepayIpn(command);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        verify(paymentRepository).save(payment);
        verify(orderRepository).save(order);
        verify(digitalLibraryService).grantPurchasedAccessForOrder(order);
        verify(orderTimelineService).recordPaymentPaid(order, payment);
    }

    @Test
    void handleSepayIpn_whenTransactionAlreadyProcessed_ignoresDuplicate() {
        paymentService = new PaymentService(
                paymentRepository,
                orderRepository,
                digitalLibraryService,
                orderTimelineService,
                new SepayProperties("merchant-123", null, "webhook-key"),
                paymentReconciliationService,
                transactionalOutboxService
        );

        Payment existingPayment = payment();
        existingPayment.markPaid("merchant-123", "TXN-001", "REF-001", "SEPAY", Instant.EPOCH.plusSeconds(1));
        HandleSepayIpnCommand command = new HandleSepayIpnCommand(
                "Apikey webhook-key",
                null,
                "TXN-001",
                "SEPAY",
                null,
                null,
                null,
                "DH123",
                "DH123",
                "in",
                null,
                new BigDecimal("10.00"),
                "REF-001",
                null
        );

        when(paymentRepository.findByTransactionId("TXN-001")).thenReturn(Optional.of(existingPayment));

        paymentService.handleSepayIpn(command);

        verify(paymentRepository, never()).findSepayByOrderCodeForUpdate(any());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(digitalLibraryService, never()).grantPurchasedAccessForOrder(any(Order.class));
    }

    @Test
    void handleSepayIpn_whenReferenceCodeAlreadySettled_ignoresDuplicate() {
        paymentService = new PaymentService(
                paymentRepository,
                orderRepository,
                digitalLibraryService,
                orderTimelineService,
                new SepayProperties("merchant-123", null, "webhook-key"),
                paymentReconciliationService,
                transactionalOutboxService
        );

        Payment existingPayment = payment();
        existingPayment.markPaid("merchant-123", "TXN-OLD", "REF-001", "SEPAY", Instant.EPOCH.plusSeconds(1));
        HandleSepayIpnCommand command = new HandleSepayIpnCommand(
                "Apikey webhook-key",
                null,
                "TXN-NEW",
                "SEPAY",
                null,
                null,
                null,
                "DH123",
                "DH123",
                "in",
                null,
                new BigDecimal("10.00"),
                "REF-001",
                null
        );

        when(paymentRepository.findByTransactionId("TXN-NEW")).thenReturn(Optional.empty());
        when(paymentRepository.findByReferenceCode("REF-001")).thenReturn(Optional.of(existingPayment));

        paymentService.handleSepayIpn(command);

        verify(paymentRepository, never()).findSepayByOrderCodeForUpdate(any());
        verify(paymentRepository, never()).findSepayByTransferContentInContentForUpdateAnyStatus(any());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(orderRepository, never()).save(any(Order.class));
        verify(digitalLibraryService, never()).grantPurchasedAccessForOrder(any(Order.class));
    }

    @Test
    void handleSepayIpn_whenWebhookSecretsMissing_rejectsFailClosed() {
        paymentService = new PaymentService(
                paymentRepository,
                orderRepository,
                digitalLibraryService,
                orderTimelineService,
                new SepayProperties("merchant-123", null, null),
                paymentReconciliationService,
                transactionalOutboxService
        );

        HandleSepayIpnCommand command = new HandleSepayIpnCommand(
                null,
                null,
                "TXN-001",
                "SEPAY",
                null,
                null,
                null,
                "DH123",
                "DH123",
                "in",
                null,
                new BigDecimal("10.00"),
                "REF-001",
                null
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> paymentService.handleSepayIpn(command)
        );

        assertEquals(ApplicationErrorCode.PAYMENT_WEBHOOK_UNAUTHORIZED, exception.getErrorCode());
        verifyNoInteractions(paymentRepository, orderRepository, digitalLibraryService, orderTimelineService);
    }

    @Test
    void handleSepayIpn_afterPaymentExpiry_createsIssueWithoutRevivingOrder() {
        paymentService = new PaymentService(
                paymentRepository, orderRepository, digitalLibraryService, orderTimelineService,
                new SepayProperties("merchant-123", null, "webhook-key"), paymentReconciliationService, transactionalOutboxService
        );
        Payment payment = payment();
        payment.markExpired(Instant.EPOCH.plusSeconds(2));
        Order order = order(payment.getOrderId());
        HandleSepayIpnCommand command = new HandleSepayIpnCommand(
                "Apikey webhook-key", null, "TXN-LATE", "SEPAY", null, null, null,
                "DH123", "DH123", "in", null, new BigDecimal("10.00"), "REF-LATE", null
        );
        when(paymentRepository.findByTransactionId("TXN-LATE")).thenReturn(Optional.empty());
        when(paymentRepository.findByReferenceCode("REF-LATE")).thenReturn(Optional.empty());
        when(paymentRepository.findSepayByOrderCodeForUpdate("DH123")).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdForUpdate(payment.getOrderId())).thenReturn(Optional.of(order));

        paymentService.handleSepayIpn(command);

        verify(paymentReconciliationService).recordIssue(
                payment, order, PaymentReconciliationIssueType.PAYMENT_AFTER_EXPIRY,
                new BigDecimal("10.00"), "TXN-LATE", "SePay xác nhận tiền sau khi đơn hàng không còn ở trạng thái có thể thanh toán"
        );
        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        assertEquals("TXN-LATE", payment.getTransactionId());
        verify(paymentRepository).save(payment);
        verify(orderRepository, never()).save(any(Order.class));
        verify(digitalLibraryService, never()).grantPurchasedAccessForOrder(any(Order.class));
    }

    private static Payment payment() {
        Instant now = Instant.EPOCH;
        return new Payment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                PaymentProvider.SEPAY,
                PaymentStatus.PENDING,
                new BigDecimal("10.00"),
                "merchant-123",
                null,
                "DH123",
                "DH123",
                null,
                null,
                now,
                now
        );
    }

    private static Order order(UUID orderId) {
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
                orderId,
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
                PaymentMethod.BANK_TRANSFER_QR,
                PaymentStatus.PENDING,
                OrderStatus.PENDING,
                "Receiver Name",
                "0123456789",
                "Receiver Address",
                now,
                now,
                null
        );
    }
}
