package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.OrderAssembler;
import com.bookstore.bookstore.application.command.CreateOrderCommand;
import com.bookstore.bookstore.application.command.UpdateOrderStatusCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.application.port.out.ICouponUsageRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.port.out.IUserAddressRepository;
import com.bookstore.bookstore.application.result.CreateOrderResult;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.domain.model.CouponUsage;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.domain.model.UserAddress;
import com.bookstore.bookstore.infrastructure.payment.SepayProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private ICartRepository cartRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IDigitalAssetRepository digitalAssetRepository;

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private IUserAddressRepository userAddressRepository;

    @Mock
    private ICouponRepository couponRepository;

    @Mock
    private ICouponUsageRepository couponUsageRepository;

    @Mock
    private IStockMovementRepository stockMovementRepository;

    @Mock
    private INotificationService notificationService;

    @Mock
    private OrderAssembler orderAssembler;

    @Mock
    private IDigitalLibraryService digitalLibraryService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                cartRepository,
                bookRepository,
                digitalAssetRepository,
                paymentRepository,
                userAddressRepository,
                couponRepository,
                couponUsageRepository,
                stockMovementRepository,
                notificationService,
                orderAssembler,
                digitalLibraryService,
                new SepayProperties("merchant-123", null, null)
        );
    }

    @Test
    void checkout_physicalOrder_decreasesStockAndClearsCart() {
        UUID userId = UUID.randomUUID();
        Book book = book(new BigDecimal("10.00"), 10);
        UserAddress address = userAddress(userId);
        Cart cart = cart(userId);
        cart.addPhysicalItem(book.getId(), 2, book.getStockQuantity());
        CreateOrderCommand command = new CreateOrderCommand(
                userId,
                List.of(),
                address.getId(),
                ShippingMethod.DELIVERY,
                PaymentMethod.BANK_TRANSFER_QR,
                null,
                null,
                null
        );

        when(userAddressRepository.findByIdAndUserIdActive(address.getId(), userId)).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(bookRepository.findAllByIdsIncludingDeletedForUpdate(List.of(book.getId()))).thenReturn(List.of(book));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(org.mockito.ArgumentMatchers.any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(org.mockito.ArgumentMatchers.any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(org.mockito.ArgumentMatchers.any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderResult result = orderService.checkout(command);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(OrderStatus.PENDING, orderCaptor.getValue().getStatus());
        assertEquals(PurchaseItemType.PHYSICAL_BOOK, orderCaptor.getValue().getItems().get(0).getItemType());
        assertEquals(new BigDecimal("30020.00"), orderCaptor.getValue().getTotalAmount());
        assertEquals(new BigDecimal("30000"), orderCaptor.getValue().getShippingFee());
        assertNotNull(orderCaptor.getValue().getOrderCode());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(PaymentProvider.SEPAY, paymentCaptor.getValue().getProvider());
        assertEquals(PaymentStatus.PENDING, paymentCaptor.getValue().getStatus());
        assertEquals(new BigDecimal("30020.00"), paymentCaptor.getValue().getAmount());
        assertEquals(orderCaptor.getValue().getOrderCode(), paymentCaptor.getValue().getReferenceCode());
        assertEquals(orderCaptor.getValue().getOrderCode(), paymentCaptor.getValue().getTransferContent());

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());
        assertEquals(8, bookCaptor.getValue().getStockQuantity());

        ArgumentCaptor<StockMovement> stockCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(stockCaptor.capture());
        assertEquals(StockMovementType.SALE, stockCaptor.getValue().getType());
        assertEquals(2, stockCaptor.getValue().getQuantity());
        assertEquals(10, stockCaptor.getValue().getBeforeQuantity());
        assertEquals(8, stockCaptor.getValue().getAfterQuantity());

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        assertEquals(0, cartCaptor.getValue().getItems().size());
        verify(notificationService).create(org.mockito.ArgumentMatchers.any());
        verify(digitalLibraryService, never()).grantPurchasedAccessForOrder(org.mockito.ArgumentMatchers.any());

        assertEquals(orderCaptor.getValue().getId(), result.orderId());
        assertEquals(PaymentMethod.BANK_TRANSFER_QR, result.paymentMethod());
        assertEquals(PaymentStatus.PENDING, result.paymentStatus());
        assertEquals(new BigDecimal("30020.00"), result.totalAmount());
    }

    @Test
    void checkout_digitalOnly_allowsMissingAddressAndCreatesNoShipping() {
        UUID userId = UUID.randomUUID();
        Book book = book(new BigDecimal("20.00"), 5);
        UUID digitalAssetId = UUID.randomUUID();
        DigitalAsset digitalAsset = digitalAsset(
                book.getId(),
                digitalAssetId,
                new BigDecimal("5.00"),
                true,
                true,
                true
        );
        Cart cart = cart(userId);
        cart.addDigitalItem(digitalAssetId);
        CreateOrderCommand command = new CreateOrderCommand(
                userId,
                List.of(),
                null,
                ShippingMethod.PICKUP,
                PaymentMethod.BANK_TRANSFER_QR,
                null,
                null,
                null
        );

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(digitalAssetRepository.findAllByIdsActive(List.of(digitalAssetId))).thenReturn(List.of(digitalAsset));
        when(bookRepository.findAllByIdsIncludingDeleted(List.of(book.getId()))).thenReturn(List.of(book));
        when(bookRepository.findAllByIdsIncludingDeletedForUpdate(List.of(book.getId()))).thenReturn(List.of(book));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(org.mockito.ArgumentMatchers.any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderResult result = orderService.checkout(command);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(PurchaseItemType.DIGITAL_ASSET, savedOrder.getItems().get(0).getItemType());
        assertEquals(digitalAssetId, savedOrder.getItems().get(0).getDigitalAssetId());
        assertEquals(BigDecimal.ZERO, savedOrder.getShippingFee());
        assertEquals(new BigDecimal("5.00"), savedOrder.getTotalAmount());
        assertEquals("Khách mua thư viện số", savedOrder.getReceiverName());
        assertEquals("Đơn hàng thư viện số", savedOrder.getReceiverAddress());

        verifyNoInteractions(userAddressRepository);
        verifyNoInteractions(stockMovementRepository);
        verify(bookRepository, never()).save(org.mockito.ArgumentMatchers.any(Book.class));
        verify(notificationService).create(org.mockito.ArgumentMatchers.any());
        assertEquals(new BigDecimal("5.00"), result.totalAmount());
        assertEquals(PaymentStatus.PENDING, result.paymentStatus());
    }

    @Test
    void checkout_withBookCoupon_persistsCouponUsageDiscountAmount() {
        UUID userId = UUID.randomUUID();
        UUID couponId = UUID.randomUUID();
        Book book = book(new BigDecimal("20.00"), 5);
        UserAddress address = userAddress(userId);
        Cart cart = cart(userId);
        cart.addPhysicalItem(book.getId(), 1, book.getStockQuantity());
        Coupon coupon = coupon(couponId, "BOOK10", 0);
        CreateOrderCommand command = new CreateOrderCommand(
                userId,
                List.of(),
                address.getId(),
                ShippingMethod.DELIVERY,
                PaymentMethod.COD,
                "BOOK10",
                null,
                null
        );

        when(userAddressRepository.findByIdAndUserIdActive(address.getId(), userId)).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(bookRepository.findAllByIdsIncludingDeletedForUpdate(List.of(book.getId()))).thenReturn(List.of(book));
        when(couponRepository.findByCodeActiveForUpdate("BOOK10")).thenReturn(Optional.of(coupon));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(org.mockito.ArgumentMatchers.any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(couponRepository.save(org.mockito.ArgumentMatchers.any(Coupon.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(couponUsageRepository.save(org.mockito.ArgumentMatchers.any(CouponUsage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(org.mockito.ArgumentMatchers.any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(org.mockito.ArgumentMatchers.any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderResult result = orderService.checkout(command);

        ArgumentCaptor<CouponUsage> couponUsageCaptor = ArgumentCaptor.forClass(CouponUsage.class);
        verify(couponUsageRepository).save(couponUsageCaptor.capture());
        assertEquals(couponId, couponUsageCaptor.getValue().getCouponId());
        assertEquals(new BigDecimal("10.00"), couponUsageCaptor.getValue().getDiscountAmount());
        assertEquals(PaymentMethod.COD, result.paymentMethod());
        assertEquals(PaymentStatus.PENDING, result.paymentStatus());
        assertEquals(new BigDecimal("30010.00"), result.totalAmount());
    }

    @Test
    void updateStatus_deliveredCod_marksPaymentPaidAndGrantsDigitalAccess() {
        UUID userId = UUID.randomUUID();
        Book book = book(new BigDecimal("20.00"), 5);
        UUID digitalAssetId = UUID.randomUUID();
        Order order = order(
                userId,
                List.of(new OrderItem(
                        UUID.randomUUID(),
                        PurchaseItemType.DIGITAL_ASSET,
                        book.getId(),
                        digitalAssetId,
                        book.getTitle(),
                        new BigDecimal("5.00"),
                        1,
                        new BigDecimal("5.00")
                )),
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                OrderStatus.SHIPPING,
                BigDecimal.ZERO
        );
        Payment payment = new Payment(
                UUID.randomUUID(),
                order.getId(),
                PaymentProvider.COD,
                PaymentStatus.PENDING,
                order.getTotalAmount(),
                null,
                null,
                order.getOrderCode(),
                order.getOrderCode(),
                null,
                null,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(org.mockito.ArgumentMatchers.any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderAssembler.toResult(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> toResult(invocation.getArgument(0)));

        OrderResult result = orderService.updateStatus(new UpdateOrderStatusCommand(order.getId(), OrderStatus.DELIVERED));

        verify(paymentRepository).save(payment);
        verify(digitalLibraryService).grantPurchasedAccessForOrder(order);
        verify(notificationService).create(org.mockito.ArgumentMatchers.any());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(OrderStatus.DELIVERED, result.status());
        assertEquals(PaymentStatus.PAID, result.paymentStatus());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"CONFIRMED", "SHIPPING", "DELIVERED"})
    void updateStatus_bankTransferQrPendingPayment_rejectsProtectedStatuses(OrderStatus nextStatus) {
        UUID userId = UUID.randomUUID();
        Order order = order(
                userId,
                List.of(new OrderItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Book Title",
                        new BigDecimal("10.00"),
                        1,
                        new BigDecimal("10.00")
                )),
                PaymentMethod.BANK_TRANSFER_QR,
                PaymentStatus.PENDING,
                OrderStatus.PENDING,
                BigDecimal.ZERO
        );

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> orderService.updateStatus(new UpdateOrderStatusCommand(order.getId(), nextStatus))
        );

        assertEquals(ApplicationErrorCode.ORDER_PAYMENT_NOT_PAID, exception.getErrorCode());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any(Order.class));
        verifyNoInteractions(paymentRepository, couponRepository, couponUsageRepository, stockMovementRepository);
    }

    @Test
    void updateStatus_cancelled_restoresPhysicalStockAndRevokesAccess() {
        UUID userId = UUID.randomUUID();
        Book book = book(new BigDecimal("10.00"), 8);
        Order order = order(
                userId,
                List.of(new OrderItem(
                        UUID.randomUUID(),
                        PurchaseItemType.PHYSICAL_BOOK,
                        book.getId(),
                        null,
                        book.getTitle(),
                        new BigDecimal("10.00"),
                        2,
                        new BigDecimal("20.00")
                )),
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                OrderStatus.PENDING,
                new BigDecimal("30000")
        );

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(bookRepository.findAllByIdsIncludingDeletedForUpdate(List.of(book.getId()))).thenReturn(List.of(book));
        when(stockMovementRepository.save(org.mockito.ArgumentMatchers.any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(org.mockito.ArgumentMatchers.any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderAssembler.toResult(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> toResult(invocation.getArgument(0)));

        OrderResult result = orderService.updateStatus(new UpdateOrderStatusCommand(order.getId(), OrderStatus.CANCELLED));

        ArgumentCaptor<StockMovement> stockCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(stockCaptor.capture());
        assertEquals(StockMovementType.CANCEL_ORDER, stockCaptor.getValue().getType());
        assertEquals(8, stockCaptor.getValue().getBeforeQuantity());
        assertEquals(10, stockCaptor.getValue().getAfterQuantity());

        verify(bookRepository).save(book);
        verify(digitalLibraryService).revokePurchasedAccessForOrder(order.getId());
        verify(notificationService).create(org.mockito.ArgumentMatchers.any());
        assertEquals(10, book.getStockQuantity());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(order.getCancelledAt());
        assertEquals(OrderStatus.CANCELLED, result.status());
    }

    @Test
    void updateStatus_cancelled_rollsBackCouponUsageOncePerAppliedCoupon() {
        UUID userId = UUID.randomUUID();
        UUID bookCouponId = UUID.randomUUID();
        UUID shippingCouponId = UUID.randomUUID();
        Order order = new Order(
                UUID.randomUUID(),
                "DH-TEST-COUPON",
                userId,
                List.of(new OrderItem(
                        UUID.randomUUID(),
                        PurchaseItemType.DIGITAL_ASSET,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Ebook",
                        new BigDecimal("20.00"),
                        1,
                        new BigDecimal("20.00")
                )),
                new BigDecimal("20.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("20.00"),
                bookCouponId,
                "BOOK10",
                shippingCouponId,
                "SHIP10",
                PaymentMethod.COD,
                PaymentStatus.PENDING,
                OrderStatus.PENDING,
                "Receiver Name",
                "0900000000",
                "Receiver Address",
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
        Coupon bookCoupon = coupon(bookCouponId, "BOOK10", 3);
        Coupon shippingCoupon = coupon(shippingCouponId, "SHIP10", 2);

        when(orderRepository.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(couponRepository.findByIdIncludingDeletedForUpdate(bookCouponId)).thenReturn(Optional.of(bookCoupon));
        when(couponRepository.findByIdIncludingDeletedForUpdate(shippingCouponId)).thenReturn(Optional.of(shippingCoupon));
        when(couponRepository.save(org.mockito.ArgumentMatchers.any(Coupon.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderAssembler.toResult(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> toResult(invocation.getArgument(0)));

        OrderResult result = orderService.updateStatus(new UpdateOrderStatusCommand(order.getId(), OrderStatus.CANCELLED));

        assertEquals(2, bookCoupon.getUsedCount());
        assertEquals(1, shippingCoupon.getUsedCount());
        verify(couponRepository).save(bookCoupon);
        verify(couponRepository).save(shippingCoupon);
        verify(couponUsageRepository).deleteByOrderId(order.getId());
        assertEquals(OrderStatus.CANCELLED, result.status());
    }

    private static Cart cart(UUID userId) {
        Instant now = Instant.EPOCH;
        return new Cart(
                UUID.randomUUID(),
                userId,
                List.of(),
                now,
                now
        );
    }

    private static UserAddress userAddress(UUID userId) {
        Instant now = Instant.EPOCH;
        return new UserAddress(
                UUID.randomUUID(),
                userId,
                "Nguyen Van A",
                "0900000000",
                "123 Test Street",
                true,
                now,
                now,
                null
        );
    }

    private static Book book(BigDecimal price, int stockQuantity) {
        Instant now = Instant.EPOCH;
        return new Book(
                UUID.randomUUID(),
                "Book Title",
                "ISBN-123",
                "Book Description",
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

    private static DigitalAsset digitalAsset(
            UUID bookId,
            UUID digitalAssetId,
            BigDecimal price,
            boolean downloadAllowed,
            boolean purchaseAllowed,
            boolean published
    ) {
        Instant now = Instant.EPOCH;
        return new DigitalAsset(
                digitalAssetId,
                bookId,
                DigitalAssetFormat.PDF,
                "Bản PDF",
                fileAsset(FilePurpose.EBOOK_FILE, "ebook.pdf", "private/digital/ebook.pdf"),
                null,
                price,
                downloadAllowed,
                purchaseAllowed,
                published,
                now,
                now,
                null
        );
    }

    private static FileAsset fileAsset(FilePurpose purpose, String originalName, String storageKey) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                UUID.randomUUID(),
                FileProvider.R2,
                purpose,
                "private-bucket",
                storageKey,
                null,
                originalName,
                "application/pdf",
                1_024L,
                "checksum",
                FileVisibility.PRIVATE,
                FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }

    private static Order order(
            UUID userId,
            List<OrderItem> items,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            OrderStatus status,
            BigDecimal shippingFee
    ) {
        Instant now = Instant.EPOCH;
        BigDecimal productTotal = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = productTotal.add(shippingFee);
        return new Order(
                UUID.randomUUID(),
                "DH-TEST-001",
                userId,
                items,
                productTotal,
                shippingFee,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                totalAmount,
                null,
                null,
                null,
                null,
                paymentMethod,
                paymentStatus,
                status,
                "Receiver Name",
                "0900000000",
                "Receiver Address",
                now,
                now,
                null
        );
    }

    private static OrderResult toResult(Order order) {
        return new OrderResult(
                order.getId(),
                order.getOrderCode(),
                order.getUserId(),
                List.of(),
                order.getProductTotal(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getShippingFee(),
                order.getShippingDiscount(),
                order.getCouponDiscount(),
                order.getFinalAmount(),
                order.getCouponId(),
                order.getCouponCode(),
                order.getBookCouponId(),
                order.getBookCouponCode(),
                order.getShippingCouponId(),
                order.getShippingCouponCode(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getStatus(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCancelledAt()
        );
    }

    private static Coupon coupon(UUID couponId, String code, int usedCount) {
        Instant now = Instant.now();
        return new Coupon(
                couponId,
                code,
                "Test coupon",
                com.bookstore.bookstore.domain.enums.CouponType.BOOK,
                com.bookstore.bookstore.domain.enums.CouponDiscountType.FIXED_AMOUNT,
                new BigDecimal("10.00"),
                BigDecimal.ZERO,
                null,
                100,
                usedCount,
                now.minusSeconds(60),
                now.plusSeconds(3_600),
                true,
                now.minusSeconds(120),
                now.minusSeconds(120),
                null
        );
    }
}
