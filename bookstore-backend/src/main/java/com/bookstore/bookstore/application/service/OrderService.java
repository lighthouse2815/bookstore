package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.OrderAssembler;
import com.bookstore.bookstore.application.command.CheckoutCommand;
import com.bookstore.bookstore.application.command.UpdateOrderStatusCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IOrderService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.application.port.out.ICouponUsageRepository;
import com.bookstore.bookstore.application.port.out.INotificationRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.port.out.IUserAddressRepository;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.domain.model.CouponUsage;
import com.bookstore.bookstore.domain.model.Notification;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.domain.model.UserAddress;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final IOrderRepository orderRepository;
    private final ICartRepository cartRepository;
    private final IBookRepository bookRepository;
    private final IUserAddressRepository userAddressRepository;
    private final ICouponRepository couponRepository;
    private final ICouponUsageRepository couponUsageRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final INotificationRepository notificationRepository;
    private final OrderAssembler orderAssembler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResult checkout(CheckoutCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UserAddress userAddress = userAddressRepository.findByIdAndUserIdActive(command.addressId(), command.userId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_ADDRESS_NOT_FOUND));

        Cart cart = cartRepository.findByUserId(command.userId())
                .filter(currentCart -> !currentCart.getItems().isEmpty())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CART_EMPTY));

        Map<UUID, Book> booksById = loadCheckoutBooks(cart);
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();
        List<OrderItem> orderItems = new ArrayList<>();
        List<StockMovement> stockMovements = new ArrayList<>();

        for (var cartItem : cart.getItems()) {
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

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Coupon coupon = resolveCoupon(command.couponCode());
        BigDecimal discountAmount = coupon == null ? BigDecimal.ZERO : coupon.applyToOrder(totalAmount, now);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        Order order = new Order(
                orderId,
                command.userId(),
                orderItems,
                totalAmount,
                discountAmount,
                BigDecimal.ZERO,
                finalAmount,
                coupon == null ? null : coupon.getId(),
                coupon == null ? null : coupon.getCode(),
                PaymentMethod.COD,
                PaymentStatus.UNPAID,
                OrderStatus.PENDING,
                userAddress.getReceiverName(),
                userAddress.getReceiverPhone(),
                userAddress.getReceiverAddress(),
                now,
                now,
                null
        );

        Order savedOrder = orderRepository.save(order);
        if (coupon != null) {
            couponRepository.save(coupon);
            couponUsageRepository.save(new CouponUsage(
                    UUID.randomUUID(),
                    coupon.getId(),
                    command.userId(),
                    orderId,
                    now
            ));
        }
        stockMovements.forEach(stockMovementRepository::save);
        booksById.values().forEach(bookRepository::save);
        cart.clear();
        cartRepository.save(cart);
        notificationRepository.save(newOrderNotification(savedOrder, now));
        return orderAssembler.toResult(savedOrder);
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
        notificationRepository.save(newOrderStatusNotification(savedOrder));
        return orderAssembler.toResult(savedOrder);
    }

    private Map<UUID, Book> loadCheckoutBooks(Cart cart) {
        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeleted(
                        cart.getItems().stream()
                                .map(item -> item.getBookId())
                                .toList()
                ).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, book) -> map.put(book.getId(), book),
                        Map::putAll
                );

        for (var cartItem : cart.getItems()) {
            Book book = booksById.get(cartItem.getBookId());
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

        if (order.getCouponId() != null) {
            Coupon coupon = couponRepository.findByIdIncludingDeleted(order.getCouponId())
                    .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.COUPON_NOT_FOUND));
            coupon.rollbackUsage(now);
            couponRepository.save(coupon);
            couponUsageRepository.deleteByOrderId(order.getId());
        }
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

    private Notification newOrderNotification(Order order, Instant now) {
        return new Notification(
                UUID.randomUUID(),
                order.getUserId(),
                "Dat hang thanh cong",
                "Don hang " + order.getId() + " da duoc tao thanh cong.",
                false,
                now,
                now,
                null,
                null
        );
    }

    private Notification newOrderStatusNotification(Order order) {
        Instant now = Instant.now();
        return new Notification(
                UUID.randomUUID(),
                order.getUserId(),
                "Cap nhat trang thai don hang",
                "Don hang " + order.getId() + " da chuyen sang " + order.getStatus().name() + ".",
                false,
                now,
                now,
                null,
                null
        );
    }
}
