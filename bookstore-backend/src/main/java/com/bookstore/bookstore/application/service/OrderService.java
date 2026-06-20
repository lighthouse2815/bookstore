package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.OrderAssembler;
import com.bookstore.bookstore.application.command.CreateOrderCommand;
import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.CreatePosOrderCommand;
import com.bookstore.bookstore.application.command.CreatePosOrderItemCommand;
import com.bookstore.bookstore.application.command.UpdateOrderStatusCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.in.IOrderService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.application.port.out.ICouponUsageRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.port.out.IUserAddressRepository;
import com.bookstore.bookstore.application.result.CreateOrderResult;
import com.bookstore.bookstore.application.result.CreatePosOrderResult;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.CouponType;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.CartItem;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.domain.model.CouponUsage;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.domain.model.UserAddress;
import com.bookstore.bookstore.infrastructure.payment.SepayProperties;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private static final BigDecimal DELIVERY_SHIPPING_FEE = BigDecimal.valueOf(30_000L);
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(200_000L);

    private final IOrderRepository orderRepository;
    private final ICartRepository cartRepository;
    private final IBookRepository bookRepository;
    private final IPaymentRepository paymentRepository;
    private final IUserAddressRepository userAddressRepository;
    private final ICouponRepository couponRepository;
    private final ICouponUsageRepository couponUsageRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final INotificationService notificationService;
    private final OrderAssembler orderAssembler;
    private final com.bookstore.bookstore.application.port.in.IDigitalLibraryService digitalLibraryService;
    private final SepayProperties sepayProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderResult checkout(CreateOrderCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Cart cart = cartRepository.findByUserId(command.userId())
                .filter(currentCart -> !currentCart.getItems().isEmpty())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CART_EMPTY));

        validatePaymentMethod(command.paymentMethod());
        UserAddress userAddress = resolveOrderAddress(command);
        List<CartItem> checkoutItems = resolveCheckoutItems(cart, command.cartItemIds());
        Map<UUID, Book> booksById = loadCheckoutBooks(checkoutItems);
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();
        String orderCode = generateOrderCode(now);
        List<OrderItem> orderItems = new ArrayList<>();
        List<StockMovement> stockMovements = new ArrayList<>();

        for (var cartItem : checkoutItems) {
            Book book = booksById.get(cartItem.getBookId());
            int beforeQuantity = book.getStockQuantity();
            book.decreaseStock(cartItem.getQuantity());
            int afterQuantity = book.getStockQuantity();

            BigDecimal lineTotal = book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItems.add(new OrderItem(
                    UUID.randomUUID(),
                    book.getId(),
                    book.getTitle(),
                    book.getPrice(),
                    cartItem.getQuantity(),
                    lineTotal
            ));
            stockMovements.add(new StockMovement(
                    UUID.randomUUID(),
                    book.getId(),
                    StockMovementType.SALE,
                    cartItem.getQuantity(),
                    beforeQuantity,
                    afterQuantity,
                    orderId,
                    "ORDER",
                    null,
                    now,
                    command.userId()
            ));
        }

        BigDecimal productTotal = orderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = calculateShippingFee(command.shippingMethod(), productTotal);
        Coupon appliedBookCoupon = resolveCoupon(command.bookCouponCode(), CouponType.BOOK);
        Coupon appliedShippingCoupon = resolveCoupon(command.shippingCouponCode(), CouponType.SHIPPING);
        BigDecimal couponDiscount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        UUID bookCouponId = null;
        String bookCouponCode = null;
        UUID shippingCouponId = null;
        String shippingCouponCode = null;

        if (appliedBookCoupon != null) {
            couponDiscount = appliedBookCoupon.applyTo(productTotal, productTotal, now);
            bookCouponId = appliedBookCoupon.getId();
            bookCouponCode = appliedBookCoupon.getCode();
        }

        if (appliedShippingCoupon != null) {
            shippingDiscount = appliedShippingCoupon.applyTo(productTotal, shippingFee, now);
            shippingCouponId = appliedShippingCoupon.getId();
            shippingCouponCode = appliedShippingCoupon.getCode();
        }

        BigDecimal totalAmount = productTotal
                .add(shippingFee)
                .subtract(shippingDiscount)
                .subtract(couponDiscount);

        Order order = new Order(
                orderId,
                orderCode,
                command.userId(),
                orderItems,
                productTotal,
                shippingFee,
                shippingDiscount,
                couponDiscount,
                totalAmount,
                bookCouponId,
                bookCouponCode,
                shippingCouponId,
                shippingCouponCode,
                command.paymentMethod(),
                PaymentStatus.PENDING,
                OrderStatus.PENDING,
                userAddress.getReceiverName(),
                userAddress.getReceiverPhone(),
                userAddress.getReceiverAddress(),
                now,
                now,
                null
        );

        Order savedOrder = orderRepository.save(order);
        Payment savedPayment = createCheckoutPayment(savedOrder, now);
        saveAppliedCoupon(appliedBookCoupon, command.userId(), orderId, now);
        saveAppliedCoupon(appliedShippingCoupon, command.userId(), orderId, now);
        stockMovements.forEach(stockMovementRepository::save);
        booksById.values().forEach(bookRepository::save);
        checkoutItems.forEach(item -> cart.removeItem(item.getBookId()));
        cartRepository.save(cart);
        notificationService.create(newOrderNotification(savedOrder));
        return new CreateOrderResult(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getPaymentMethod(),
                savedPayment.getStatus(),
                savedOrder.getTotalAmount(),
                savedPayment.getTransferContent()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreatePosOrderResult createPosOrder(CreatePosOrderCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        List<CreatePosOrderItemCommand> posItems = mergePosItems(command.items());
        Map<UUID, Book> booksById = loadPosOrderBooks(posItems);
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();
        String orderCode = generatePosOrderCode(now);
        List<OrderItem> orderItems = new ArrayList<>();
        List<StockMovement> stockMovements = new ArrayList<>();

        for (var item : posItems) {
            Book book = booksById.get(item.bookId());
            int beforeQuantity = book.getStockQuantity();
            book.decreaseStock(item.quantity());
            int afterQuantity = book.getStockQuantity();

            BigDecimal lineTotal = book.getPrice().multiply(BigDecimal.valueOf(item.quantity()));
            orderItems.add(new OrderItem(
                    UUID.randomUUID(),
                    book.getId(),
                    book.getTitle(),
                    book.getPrice(),
                    item.quantity(),
                    lineTotal
            ));
            stockMovements.add(new StockMovement(
                    UUID.randomUUID(),
                    book.getId(),
                    StockMovementType.SALE,
                    item.quantity(),
                    beforeQuantity,
                    afterQuantity,
                    orderId,
                    "POS_ORDER",
                    null,
                    now,
                    command.staffUserId()
            ));
        }

        BigDecimal productTotal = orderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Coupon requestedCoupon = resolveCoupon(command.couponCode());
        Coupon appliedBookCoupon = null;
        BigDecimal couponDiscount = BigDecimal.ZERO;
        UUID bookCouponId = null;
        String bookCouponCode = null;

        if (requestedCoupon != null) {
            switch (requestedCoupon.getCouponType()) {
                case BOOK -> {
                    couponDiscount = requestedCoupon.applyTo(productTotal, productTotal, now);
                    bookCouponId = requestedCoupon.getId();
                    bookCouponCode = requestedCoupon.getCode();
                    appliedBookCoupon = requestedCoupon;
                }
                case SHIPPING -> {
                    couponDiscount = BigDecimal.ZERO;
                }
            }
        }

        BigDecimal finalAmount = productTotal.subtract(couponDiscount);
        PaymentStatus paymentStatus = resolvePosPaymentStatus(command.paymentMethod());
        OrderStatus orderStatus = paymentStatus == PaymentStatus.PAID ? OrderStatus.DELIVERED : OrderStatus.PENDING;

        Order order = new Order(
                orderId,
                orderCode,
                command.staffUserId(),
                orderItems,
                productTotal,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                couponDiscount,
                finalAmount,
                bookCouponId,
                bookCouponCode,
                null,
                null,
                command.paymentMethod(),
                paymentStatus,
                orderStatus,
                resolvePosCustomerName(command.customerName()),
                resolvePosCustomerPhone(command.customerPhone()),
                "Tai quay",
                now,
                now,
                null
        );

        Order savedOrder = orderRepository.save(order);
        Payment savedPayment = paymentRepository.save(new Payment(
                UUID.randomUUID(),
                savedOrder.getId(),
                PaymentProvider.POS,
                paymentStatus,
                savedOrder.getTotalAmount(),
                null,
                paymentStatus == PaymentStatus.PAID ? "POS-" + savedOrder.getOrderCode() : null,
                savedOrder.getOrderCode(),
                savedOrder.getOrderCode(),
                "POS",
                paymentStatus == PaymentStatus.PAID ? now : null,
                now,
                now
        ));
        saveAppliedCoupon(appliedBookCoupon, command.staffUserId(), savedOrder.getId(), now);
        stockMovements.forEach(stockMovementRepository::save);
        booksById.values().forEach(bookRepository::save);
        notificationService.create(newOrderNotification(savedOrder));
        if (paymentStatus == PaymentStatus.PAID) {
            digitalLibraryService.grantPurchasedAccessForOrder(savedOrder);
        }

        return new CreatePosOrderResult(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getTotalAmount(),
                savedOrder.getDiscountAmount(),
                savedOrder.getFinalAmount(),
                savedOrder.getPaymentMethod(),
                savedPayment.getStatus(),
                savedOrder.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResult> getMyOrders(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderAssembler::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResult getMyOrder(UUID userId, UUID orderId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getUserId().equals(userId))
                .map(orderAssembler::toResult)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResult> getAll() {
        return orderRepository.findAll().stream()
                .map(orderAssembler::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResult getById(UUID orderId) {
        if (orderId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "orderId");
        }

        return orderRepository.findById(orderId)
                .map(orderAssembler::toResult)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResult updateStatus(UpdateOrderStatusCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Order currentOrder = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));

        currentOrder.updateStatus(command.status());
        if (command.status() == OrderStatus.CANCELLED) {
            rollbackCancelledOrder(currentOrder);
        }
        Order savedOrder = orderRepository.save(currentOrder);
        if (savedOrder.getStatus() == OrderStatus.CANCELLED) {
            digitalLibraryService.revokePurchasedAccessForOrder(savedOrder.getId());
        }
        if (savedOrder.getStatus() == OrderStatus.DELIVERED && savedOrder.getPaymentMethod() == PaymentMethod.COD) {
            digitalLibraryService.grantPurchasedAccessForOrder(savedOrder);
        }
        notificationService.create(newOrderStatusNotification(savedOrder));
        return orderAssembler.toResult(savedOrder);
    }

    private List<CartItem> resolveCheckoutItems(Cart cart, List<UUID> selectedCartItemIds) {
        if (selectedCartItemIds.isEmpty()) {
            return List.copyOf(cart.getItems());
        }

        Map<UUID, CartItem> cartItemsById = cart.getItems().stream()
                .collect(
                        LinkedHashMap::new,
                        (map, item) -> map.put(item.getId(), item),
                        Map::putAll
                );
        List<CartItem> checkoutItems = new ArrayList<>();

        for (UUID selectedCartItemId : selectedCartItemIds) {
            CartItem cartItem = cartItemsById.get(selectedCartItemId);
            if (cartItem == null) {
                throw new ApplicationException(ApplicationErrorCode.CART_ITEM_NOT_FOUND);
            }
            checkoutItems.add(cartItem);
        }

        return checkoutItems;
    }

    private List<CreatePosOrderItemCommand> mergePosItems(List<CreatePosOrderItemCommand> items) {
        Map<UUID, Integer> quantitiesByBookId = new LinkedHashMap<>();
        for (CreatePosOrderItemCommand item : items) {
            quantitiesByBookId.merge(item.bookId(), item.quantity(), Integer::sum);
        }

        return quantitiesByBookId.entrySet().stream()
                .map(entry -> new CreatePosOrderItemCommand(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Map<UUID, Book> loadCheckoutBooks(List<CartItem> checkoutItems) {
        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeleted(
                        checkoutItems.stream()
                                .map(item -> item.getBookId())
                                .toList()
                ).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, book) -> map.put(book.getId(), book),
                        Map::putAll
                );

        for (var cartItem : checkoutItems) {
            Book book = booksById.get(cartItem.getBookId());
            if (book == null || book.getDeletedAt() != null) {
                throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
            }
        }

        return booksById;
    }

    private Map<UUID, Book> loadPosOrderBooks(List<CreatePosOrderItemCommand> items) {
        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeleted(
                        items.stream()
                                .map(CreatePosOrderItemCommand::bookId)
                                .toList()
                ).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, book) -> map.put(book.getId(), book),
                        Map::putAll
                );

        for (var item : items) {
            Book book = booksById.get(item.bookId());
            if (book == null || book.getDeletedAt() != null) {
                throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
            }
        }

        return booksById;
    }

    private Coupon resolveCoupon(String couponCode) {
        String normalizedCouponCode = StringUtils.trimToNull(couponCode);
        if (normalizedCouponCode == null) {
            return null;
        }

        return couponRepository.findByCodeActive(normalizedCouponCode.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.COUPON_NOT_FOUND));
    }

    private Coupon resolveCoupon(String couponCode, CouponType expectedType) {
        Coupon coupon = resolveCoupon(couponCode);
        if (coupon == null) {
            return null;
        }

        if (coupon.getCouponType() != expectedType) {
            throw new ApplicationException(ApplicationErrorCode.COUPON_TYPE_NOT_MATCH);
        }

        return coupon;
    }

    private void saveAppliedCoupon(Coupon coupon, UUID userId, UUID orderId, Instant usedAt) {
        if (coupon == null) {
            return;
        }

        couponRepository.save(coupon);
        couponUsageRepository.save(new CouponUsage(
                UUID.randomUUID(),
                coupon.getId(),
                userId,
                orderId,
                usedAt
        ));
    }

    private void rollbackCancelledOrder(Order order) {
        Map<UUID, Book> booksById = loadOrderBooks(order);
        Instant now = Instant.now();

        for (OrderItem item : order.getItems()) {
            Book book = booksById.get(item.getBookId());
            int beforeQuantity = book.getStockQuantity();
            book.increaseStock(item.getQuantity());
            int afterQuantity = book.getStockQuantity();

            stockMovementRepository.save(new StockMovement(
                    UUID.randomUUID(),
                    book.getId(),
                    StockMovementType.CANCEL_ORDER,
                    item.getQuantity(),
                    beforeQuantity,
                    afterQuantity,
                    order.getId(),
                    "ORDER",
                    null,
                    now,
                    order.getUserId()
            ));
        }

        booksById.values().forEach(bookRepository::save);

        UUID bookCouponId = order.getBookCouponId();
        UUID shippingCouponId = order.getShippingCouponId();
        if (bookCouponId != null) {
            rollbackCouponUsage(bookCouponId, now);
        }
        if (shippingCouponId != null && !shippingCouponId.equals(bookCouponId)) {
            rollbackCouponUsage(shippingCouponId, now);
        }
        if (bookCouponId != null || shippingCouponId != null) {
            couponUsageRepository.deleteByOrderId(order.getId());
        }
    }

    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod != PaymentMethod.BANK_TRANSFER_QR
                && paymentMethod != PaymentMethod.COD) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "paymentMethod");
        }
    }

    private UserAddress resolveOrderAddress(CreateOrderCommand command) {
        if (command.shippingMethod() == ShippingMethod.DELIVERY) {
            return userAddressRepository.findByIdAndUserIdActive(command.addressId(), command.userId())
                    .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_ADDRESS_NOT_FOUND));
        }

        if (command.addressId() != null) {
            return userAddressRepository.findByIdAndUserIdActive(command.addressId(), command.userId())
                    .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_ADDRESS_NOT_FOUND));
        }

        return userAddressRepository.findAllByUserIdActive(command.userId()).stream()
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_ADDRESS_NOT_FOUND));
    }

    private BigDecimal calculateShippingFee(ShippingMethod shippingMethod, BigDecimal productTotal) {
        return switch (shippingMethod) {
            case DELIVERY -> productTotal.compareTo(FREE_SHIPPING_THRESHOLD) < 0
                    ? DELIVERY_SHIPPING_FEE
                    : BigDecimal.ZERO;
            case PICKUP -> BigDecimal.ZERO;
        };
    }

    private Payment createCheckoutPayment(Order order, Instant createdAt) {
        PaymentProvider provider = order.getPaymentMethod() == PaymentMethod.COD
                ? PaymentProvider.COD
                : PaymentProvider.SEPAY;
        String merchantId = provider == PaymentProvider.SEPAY
                ? StringUtils.trimToNull(sepayProperties.merchantId())
                : null;

        return paymentRepository.save(new Payment(
                UUID.randomUUID(),
                order.getId(),
                provider,
                PaymentStatus.PENDING,
                order.getTotalAmount(),
                merchantId,
                null,
                order.getOrderCode(),
                order.getOrderCode(),
                null,
                null,
                createdAt,
                createdAt
        ));
    }

    private String generateOrderCode(Instant now) {
        return "DH" + now.toEpochMilli() + ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
    }

    private String generatePosOrderCode(Instant now) {
        return "POS" + now.toEpochMilli() + ThreadLocalRandom.current().nextInt(10_000, 100_000);
    }

    private PaymentStatus resolvePosPaymentStatus(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case CASH, BANK_TRANSFER -> PaymentStatus.PAID;
            case COD, BANK_TRANSFER_QR -> PaymentStatus.PENDING;
        };
    }

    private String resolvePosCustomerName(String customerName) {
        return StringUtils.trimToNull(customerName) == null ? "Khach le" : customerName;
    }

    private String resolvePosCustomerPhone(String customerPhone) {
        return StringUtils.trimToNull(customerPhone) == null ? "0900000000" : customerPhone;
    }

    private void rollbackCouponUsage(UUID couponId, Instant rolledBackAt) {
        Coupon coupon = couponRepository.findByIdIncludingDeleted(couponId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.COUPON_NOT_FOUND));
        coupon.rollbackUsage(rolledBackAt);
        couponRepository.save(coupon);
    }

    private Map<UUID, Book> loadOrderBooks(Order order) {
        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeleted(
                        order.getItems().stream()
                                .map(OrderItem::getBookId)
                                .toList()
                ).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, book) -> map.put(book.getId(), book),
                        Map::putAll
                );

        for (OrderItem item : order.getItems()) {
            if (!booksById.containsKey(item.getBookId())) {
                throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
            }
        }

        return booksById;
    }

    private CreateNotificationCommand newOrderNotification(Order order) {
        return new CreateNotificationCommand(
                order.getUserId(),
                "Dat hang thanh cong",
                "Don hang " + order.getOrderCode() + " da duoc tao thanh cong.",
                "ORDER",
                "ORDER",
                order.getId(),
                "/orders/" + order.getId()
        );
    }

    private CreateNotificationCommand newOrderStatusNotification(Order order) {
        return new CreateNotificationCommand(
                order.getUserId(),
                "Cap nhat trang thai don hang",
                "Don hang " + order.getOrderCode() + " da chuyen sang " + order.getStatus().name() + ".",
                "ORDER",
                "ORDER",
                order.getId(),
                "/orders/" + order.getId()
        );
    }
}
