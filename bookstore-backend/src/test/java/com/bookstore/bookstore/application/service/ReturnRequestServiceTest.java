package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.ReturnRequestAssembler;
import com.bookstore.bookstore.application.command.ApproveReturnRequestCommand;
import com.bookstore.bookstore.application.command.CreateReturnRequestCommand;
import com.bookstore.bookstore.application.command.RejectReturnRequestCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReturnRequestRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.ReturnRequestResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Permission;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.domain.model.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReturnRequestServiceTest {

    @Mock
    private IReturnRequestRepository returnRequestRepository;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IStockMovementRepository stockMovementRepository;

    @Mock
    private INotificationService notificationService;

    @Mock
    private IOrderTimelineService orderTimelineService;

    private ReturnRequestService returnRequestService;

    @BeforeEach
    void setUp() {
        returnRequestService = new ReturnRequestService(
                returnRequestRepository,
                orderRepository,
                userRepository,
                bookRepository,
                stockMovementRepository,
                notificationService,
                orderTimelineService,
                new ReturnRequestAssembler()
        );
    }

    @Test
    void create_whenDeliveredOrderBelongsToUser_createsPendingRequest() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Order order = order(
                userId,
                List.of(physicalOrderItem(UUID.randomUUID(), 1, "100000")),
                OrderStatus.DELIVERED
        );
        User customer = user(userId, "customer", "customer@example.com", "USER");
        User admin = user(adminId, "admin", "admin@example.com", "ADMIN");

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(returnRequestRepository.existsActiveByOrderIdAndStatuses(any(), any())).thenReturn(false);
        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllActive()).thenReturn(List.of(admin));
        when(userRepository.findByIdIncludingDeleted(userId)).thenReturn(Optional.of(customer));

        ReturnRequestResult result = returnRequestService.create(new CreateReturnRequestCommand(
                order.getId(),
                userId,
                "Sách bị móp góc",
                new BigDecimal("50000")
        ));

        assertEquals(ReturnRequestStatus.PENDING, result.status());
        assertEquals(order.getOrderCode(), result.orderCode());
        assertEquals("customer", result.username());
        assertEquals(new BigDecimal("50000"), result.requestedRefundAmount());
        assertNotNull(result.id());
        verify(orderRepository).findByIdForUpdate(order.getId());
        verify(orderTimelineService).recordReturnRequested(any(Order.class), any(ReturnRequest.class));
        verify(notificationService).create(argThat(command ->
                command.userId().equals(adminId)
                        && "/admin/return-requests".equals(command.link())
        ));
    }

    @Test
    void create_whenOrderNotDelivered_throwsConflict() {
        UUID userId = UUID.randomUUID();
        Order order = order(
                userId,
                List.of(physicalOrderItem(UUID.randomUUID(), 1, "100000")),
                OrderStatus.SHIPPING
        );

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> returnRequestService.create(new CreateReturnRequestCommand(
                        order.getId(),
                        userId,
                        "Muốn trả hàng",
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.RETURN_REQUEST_ORDER_NOT_DELIVERED, exception.getErrorCode());
        verify(returnRequestRepository, never()).save(any(ReturnRequest.class));
    }

    @Test
    void create_whenOrderBelongsToOtherUser_throwsOrderNotFound() {
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        Order order = order(
                ownerId,
                List.of(physicalOrderItem(UUID.randomUUID(), 1, "100000")),
                OrderStatus.DELIVERED
        );

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> returnRequestService.create(new CreateReturnRequestCommand(
                        order.getId(),
                        attackerId,
                        "Không phải đơn của tôi",
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(returnRequestRepository);
    }

    @Test
    void create_whenDuplicatePendingRequestExists_throwsConflict() {
        UUID userId = UUID.randomUUID();
        Order order = order(
                userId,
                List.of(physicalOrderItem(UUID.randomUUID(), 1, "100000")),
                OrderStatus.DELIVERED
        );

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(returnRequestRepository.existsActiveByOrderIdAndStatuses(any(), any())).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> returnRequestService.create(new CreateReturnRequestCommand(
                        order.getId(),
                        userId,
                        "Trùng request",
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.RETURN_REQUEST_ALREADY_EXISTS, exception.getErrorCode());
        verify(returnRequestRepository, never()).save(any(ReturnRequest.class));
    }

    @Test
    void approve_whenPendingRequestAndRestockEnabled_updatesStatusAndStock() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Book book = book(new BigDecimal("100000"), 8);
        Order order = order(
                userId,
                List.of(physicalOrderItem(book.getId(), 2, "200000")),
                OrderStatus.DELIVERED
        );
        ReturnRequest returnRequest = pendingReturnRequest(order.getId(), userId, new BigDecimal("100000"));
        User customer = user(userId, "customer", "customer@example.com", "USER");
        User admin = user(adminId, "admin", "admin@example.com", "ADMIN");

        when(returnRequestRepository.findByIdActiveForUpdate(returnRequest.getId())).thenReturn(Optional.of(returnRequest));
        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(bookRepository.findAllByIdsIncludingDeletedForUpdate(List.of(book.getId()))).thenReturn(List.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByIdIncludingDeleted(userId)).thenReturn(Optional.of(customer));
        when(userRepository.findByIdIncludingDeleted(adminId)).thenReturn(Optional.of(admin));

        ReturnRequestResult result = returnRequestService.approve(new ApproveReturnRequestCommand(
                returnRequest.getId(),
                adminId,
                "Đã kiểm tra và chấp nhận",
                new BigDecimal("80000"),
                true
        ));

        assertEquals(ReturnRequestStatus.APPROVED, result.status());
        assertEquals(new BigDecimal("80000"), result.approvedRefundAmount());
        assertEquals(10, book.getStockQuantity());
        verify(orderTimelineService).recordReturnApproved(order, returnRequest);
        verify(orderTimelineService).recordRefundInternalApproved(order, returnRequest);
        verify(orderTimelineService).recordStockRestockedFromReturn(order, returnRequest, 2);
        verify(notificationService).create(argThat(command ->
                command.userId().equals(userId)
                        && "/return-requests".equals(command.link())
        ));

        ArgumentCaptor<StockMovement> stockCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(stockCaptor.capture());
        assertEquals(StockMovementType.ADJUSTMENT, stockCaptor.getValue().getType());
        assertEquals(8, stockCaptor.getValue().getBeforeQuantity());
        assertEquals(10, stockCaptor.getValue().getAfterQuantity());
        assertEquals("RETURN_REQUEST", stockCaptor.getValue().getReferenceType());
    }

    @Test
    void reject_whenPendingRequest_updatesStatusAndStoresReason() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Order order = order(
                userId,
                List.of(physicalOrderItem(UUID.randomUUID(), 1, "100000")),
                OrderStatus.DELIVERED
        );
        ReturnRequest returnRequest = pendingReturnRequest(order.getId(), userId, new BigDecimal("50000"));
        User customer = user(userId, "customer", "customer@example.com", "USER");
        User admin = user(adminId, "admin", "admin@example.com", "ADMIN");

        when(returnRequestRepository.findByIdActiveForUpdate(returnRequest.getId())).thenReturn(Optional.of(returnRequest));
        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findByIdIncludingDeleted(userId)).thenReturn(Optional.of(customer));
        when(userRepository.findByIdIncludingDeleted(adminId)).thenReturn(Optional.of(admin));

        ReturnRequestResult result = returnRequestService.reject(new RejectReturnRequestCommand(
                returnRequest.getId(),
                adminId,
                "Sản phẩm không thuộc diện hỗ trợ trả hàng"
        ));

        assertEquals(ReturnRequestStatus.REJECTED, result.status());
        assertEquals("Sản phẩm không thuộc diện hỗ trợ trả hàng", result.adminNote());
        verify(orderTimelineService).recordReturnRejected(order, returnRequest);
        verify(notificationService).create(argThat(command ->
                command.userId().equals(userId)
                        && "/return-requests".equals(command.link())
        ));
        verify(bookRepository, never()).findAllByIdsIncludingDeletedForUpdate(any());
    }

    private static ReturnRequest pendingReturnRequest(UUID orderId, UUID userId, BigDecimal requestedRefundAmount) {
        Instant now = Instant.EPOCH;
        return new ReturnRequest(
                UUID.randomUUID(),
                orderId,
                userId,
                "Yêu cầu trả hàng",
                ReturnRequestStatus.PENDING,
                null,
                requestedRefundAmount,
                null,
                null,
                null,
                now,
                now,
                null
        );
    }

    private static Order order(UUID userId, List<OrderItem> items, OrderStatus status) {
        Instant now = Instant.EPOCH;
        BigDecimal productTotal = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(
                UUID.randomUUID(),
                "DH-RETURN-001",
                userId,
                items,
                productTotal,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                productTotal,
                null,
                null,
                null,
                null,
                PaymentMethod.COD,
                PaymentStatus.PAID,
                status,
                "Nguyen Van A",
                "0900000000",
                "123 Test Street",
                now,
                now,
                null
        );
    }

    private static OrderItem physicalOrderItem(UUID bookId, int quantity, String lineTotal) {
        return new OrderItem(
                UUID.randomUUID(),
                PurchaseItemType.PHYSICAL_BOOK,
                bookId,
                null,
                "Book Title",
                new BigDecimal(lineTotal).divide(BigDecimal.valueOf(quantity)),
                quantity,
                new BigDecimal(lineTotal)
        );
    }

    private static Book book(BigDecimal price, int stockQuantity) {
        Instant now = Instant.EPOCH;
        return new Book(
                UUID.randomUUID(),
                "Book Title",
                "ISBN-123",
                "Description",
                price,
                stockQuantity,
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }

    private static User user(UUID userId, String username, String email, String roleName) {
        Instant now = Instant.EPOCH;
        return new User(
                userId,
                username,
                "hashed-password",
                "0900000000",
                email,
                com.bookstore.bookstore.domain.enums.UserStatus.ACTIVE,
                false,
                Set.of(role(roleName)),
                now,
                now,
                null
        );
    }

    private static Role role(String roleName) {
        Instant now = Instant.EPOCH;
        return new Role(
                UUID.randomUUID(),
                roleName,
                roleName + " role",
                Set.<Permission>of(),
                now,
                now,
                null
        );
    }
}
