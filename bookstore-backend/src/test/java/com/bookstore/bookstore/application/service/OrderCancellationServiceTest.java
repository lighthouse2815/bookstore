package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Payment;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderCancellationServiceTest {

    @Mock private IPaymentRepository paymentRepository;
    @Mock private IOrderRepository orderRepository;
    @Mock private IBookRepository bookRepository;
    @Mock private ICouponRepository couponRepository;
    @Mock private ICouponUsageRepository couponUsageRepository;
    @Mock private IStockMovementRepository stockMovementRepository;
    @Mock private ITransactionalOutboxService transactionalOutboxService;
    @Mock private IOrderTimelineService orderTimelineService;
    @Mock private IDigitalLibraryService digitalLibraryService;
    @Mock private IAuditLogService auditLogService;

    @InjectMocks private OrderCancellationService service;

    @Test
    void ownerCancel_pendingOrder_rollsBackPhysicalStockExactlyOnce() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Order order = pendingOrder(orderId, userId);
        Payment payment = pendingPayment(orderId);
        OrderItem item = org.mockito.Mockito.mock(OrderItem.class);
        Book book = org.mockito.Mockito.mock(Book.class);
        when(item.getItemType()).thenReturn(PurchaseItemType.PHYSICAL_BOOK);
        when(item.getBookId()).thenReturn(bookId);
        when(item.getQuantity()).thenReturn(2);
        when(order.getItems()).thenReturn(List.of(item));
        when(book.getId()).thenReturn(bookId);
        AtomicInteger stock = new AtomicInteger(3);
        when(book.getStockQuantity()).thenAnswer(invocation -> stock.get());
        org.mockito.Mockito.doAnswer(invocation -> {
            stock.addAndGet(invocation.getArgument(0));
            return null;
        }).when(book).increaseStock(2);
        when(paymentRepository.findByOrderIdForUpdate(orderId)).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(bookRepository.findAllByIdsIncludingDeletedForUpdate(List.of(bookId))).thenReturn(List.of(book));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(orderRepository.save(order)).thenReturn(order);

        service.cancelOwnedPending(userId, orderId, "Không còn nhu cầu");

        verify(payment).markCancelled(any(Instant.class));
        verify(order).cancel(any(Instant.class));
        verify(order).markPaymentCancelled(any(Instant.class));
        verify(book).increaseStock(2);
        verify(stockMovementRepository).save(any());
        verify(couponUsageRepository, never()).deleteByOrderId(orderId);
        verify(orderTimelineService).recordOrderCancelled(order, "Không còn nhu cầu");
        verify(orderTimelineService).recordStockRolledBack(order);
    }

    @Test
    void userOtherThanOwner_cannotCancelAndDoesNotRollBack() {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = pendingPayment(orderId);
        Order order = pendingOrder(orderId, ownerId);
        when(paymentRepository.findByOrderIdForUpdate(orderId)).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> service.cancelOwnedPending(UUID.randomUUID(), orderId, "Không còn nhu cầu")
        );

        org.junit.jupiter.api.Assertions.assertEquals(ApplicationErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        verify(payment, never()).markCancelled(any());
        verify(order, never()).cancel();
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void expiry_skipsPaymentThatWasAlreadyPaid() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = org.mockito.Mockito.mock(Payment.class);
        when(payment.getStatus()).thenReturn(PaymentStatus.PAID);
        when(paymentRepository.findByIdForUpdate(paymentId)).thenReturn(Optional.of(payment));

        boolean expired = service.expirePendingPayment(paymentId, Instant.now());

        assertFalse(expired);
        verify(orderRepository, never()).findByIdForUpdate(any());
        verify(payment, never()).markExpired(any());
    }

    @Test
    void expiry_pendingPayment_marksExpiredAndCancelsOrder() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = pendingPayment(orderId);
        Order order = pendingOrder(orderId, UUID.randomUUID());
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getExpiresAt()).thenReturn(Instant.now().minusSeconds(1));
        when(paymentRepository.findByIdForUpdate(paymentId)).thenReturn(Optional.of(payment));
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(orderRepository.save(order)).thenReturn(order);

        boolean expired = service.expirePendingPayment(paymentId, Instant.now());

        assertTrue(expired);
        verify(payment).markExpired(any(Instant.class));
        verify(order).cancel(any(Instant.class));
        verify(order).markPaymentExpired(any(Instant.class));
        verify(stockMovementRepository, never()).save(any());
    }

    private static Payment pendingPayment(UUID orderId) {
        Payment payment = org.mockito.Mockito.mock(Payment.class);
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(payment.getOrderId()).thenReturn(orderId);
        return payment;
    }

    private static Order pendingOrder(UUID orderId, UUID userId) {
        Order order = org.mockito.Mockito.mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getUserId()).thenReturn(userId);
        when(order.getStatus()).thenReturn(OrderStatus.PENDING);
        when(order.getPaymentStatus()).thenReturn(PaymentStatus.PENDING);
        when(order.getOrderCode()).thenReturn("DH-TEST");
        when(order.getItems()).thenReturn(List.of());
        return order;
    }
}
