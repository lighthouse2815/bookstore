package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.ShipmentAssembler;
import com.bookstore.bookstore.application.command.AssignShipmentCommand;
import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.UpdateShipmentStatusCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.in.IShipmentService;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.application.port.out.IShipmentRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ShipmentResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.Shipment;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentService implements IShipmentService {

    private static final String SHIPPER_ROLE = "SHIPPER";

    private final IShipmentRepository shipmentRepository;
    private final IOrderRepository orderRepository;
    private final IPaymentRepository paymentRepository;
    private final IUserRepository userRepository;
    private final INotificationService notificationService;
    private final ShipmentAssembler shipmentAssembler;
    private final IDigitalLibraryService digitalLibraryService;
    private final IOrderTimelineService orderTimelineService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShipmentResult assign(AssignShipmentCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Order order = orderRepository.findByIdForUpdate(command.orderId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        requireOrderReady(order);
        requireNoActiveAssignment(command.orderId());
        User shipper = loadShipper(command.shipperId());
        OrderStatus oldOrderStatus = order.getStatus();

        Order orderForResponse = order;
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            order.startShipping();
            orderForResponse = orderRepository.save(order);
        }

        Instant now = Instant.now();
        Shipment shipment = new Shipment(
                UUID.randomUUID(),
                orderForResponse.getId(),
                shipper.getId(),
                ShipmentStatus.ASSIGNED,
                null,
                now,
                now,
                null,
                null,
                null,
                null
        );
        Shipment savedShipment = shipmentRepository.save(shipment);
        notificationService.create(newShipmentAssignedNotification(orderForResponse, shipper));
        if (oldOrderStatus != orderForResponse.getStatus()) {
            orderTimelineService.recordStatusChanged(orderForResponse, oldOrderStatus, orderForResponse.getStatus());
        }
        orderTimelineService.recordShipmentAssigned(orderForResponse, savedShipment, shipper.getUsername());
        return shipmentAssembler.toResult(savedShipment, orderForResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResult> getAll() {
        return shipmentRepository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ShipmentResult> getAll(PageQuery pageQuery) {
        return shipmentRepository.findPageAll(pageQuery.page(), pageQuery.size()).map(this::toResult);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResult getById(UUID shipmentId) {
        if (shipmentId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipmentId");
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHIPMENT_NOT_FOUND));
        return toResult(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResult> getMyShipments(UUID shipperId) {
        if (shipperId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipperId");
        }

        return shipmentRepository.findAllByShipperId(shipperId).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ShipmentResult> getMyShipments(UUID shipperId, PageQuery pageQuery) {
        if (shipperId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipperId");
        }

        return shipmentRepository.findPageByShipperId(shipperId, pageQuery.page(), pageQuery.size())
                .map(this::toResult);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResult getMyShipment(UUID shipperId, UUID shipmentId) {
        if (shipperId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipperId");
        }
        if (shipmentId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipmentId");
        }

        Shipment shipment = shipmentRepository.findByIdAndShipperId(shipmentId, shipperId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHIPMENT_NOT_FOUND));
        return toResult(shipment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShipmentResult updateMyShipmentStatus(UpdateShipmentStatusCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Shipment shipment = shipmentRepository.findByIdAndShipperId(command.shipmentId(), command.shipperId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHIPMENT_NOT_FOUND));
        ShipmentStatus oldShipmentStatus = shipment.getStatus();

        switch (command.status()) {
            case ASSIGNED -> throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "status");
            case PICKED_UP -> {
                Order order = loadOrder(shipment.getOrderId());
                shipment.markPickedUp();
                Shipment savedShipment = shipmentRepository.save(shipment);
                notificationService.create(newShipmentStatusNotification(order, savedShipment));
                orderTimelineService.recordShipmentStatusChanged(order, savedShipment, oldShipmentStatus, savedShipment.getStatus());
                return shipmentAssembler.toResult(savedShipment, order);
            }
            case DELIVERING -> {
                Order order = loadOrder(shipment.getOrderId());
                shipment.startDelivering();
                Shipment savedShipment = shipmentRepository.save(shipment);
                notificationService.create(newShipmentStatusNotification(order, savedShipment));
                orderTimelineService.recordShipmentStatusChanged(order, savedShipment, oldShipmentStatus, savedShipment.getStatus());
                return shipmentAssembler.toResult(savedShipment, order);
            }
            case DELIVERED -> {
                return completeDeliveredShipment(shipment, loadOrderForUpdate(shipment.getOrderId()), false);
            }
            case FAILED -> {
                Order order = loadOrder(shipment.getOrderId());
                shipment.markFailed(StringUtils.trimToNull(command.failureReason()));
                Shipment savedShipment = shipmentRepository.save(shipment);
                notificationService.create(newShipmentStatusNotification(order, savedShipment));
                orderTimelineService.recordShipmentStatusChanged(order, savedShipment, oldShipmentStatus, savedShipment.getStatus());
                return shipmentAssembler.toResult(savedShipment, order);
            }
        }

        throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "status");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShipmentResult confirmDeliveredByAdmin(UUID shipmentId) {
        if (shipmentId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "shipmentId");
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHIPMENT_NOT_FOUND));
        Order order = loadOrderForUpdate(shipment.getOrderId());
        return completeDeliveredShipment(shipment, order, true);
    }

    private ShipmentResult toResult(Shipment shipment) {
        return shipmentAssembler.toResult(shipment, loadOrder(shipment.getOrderId()));
    }

    private Order loadOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
    }

    private Order loadOrderForUpdate(UUID orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
    }

    private User loadShipper(UUID shipperId) {
        return userRepository.findByIdActive(shipperId)
                .filter(user -> user.hasRole(SHIPPER_ROLE))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHIPPER_NOT_FOUND));
    }

    private void requireOrderReady(Order order) {
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.SHIPPING) {
            throw new ApplicationException(ApplicationErrorCode.SHIPMENT_ORDER_NOT_READY);
        }
    }

    private void requireNoActiveAssignment(UUID orderId) {
        boolean hasActiveAssignment = shipmentRepository.findAllByOrderId(orderId).stream()
                .anyMatch(Shipment::isOngoing);

        if (hasActiveAssignment) {
            throw new ApplicationException(ApplicationErrorCode.SHIPMENT_ORDER_ALREADY_HAS_ACTIVE_ASSIGNMENT);
        }
    }

    private CreateNotificationCommand newShipmentAssignedNotification(Order order, User shipper) {
        return new CreateNotificationCommand(
                order.getUserId(),
                "Đơn hàng đã được giao shipper",
                "Đơn hàng " + order.getOrderCode() + " đã được giao cho shipper " + shipper.getUsername() + ".",
                "SHIPMENT",
                "ORDER",
                order.getId(),
                "/orders/" + order.getId()
        );
    }

    private CreateNotificationCommand newShipmentStatusNotification(Order order, Shipment shipment) {
        return new CreateNotificationCommand(
                order.getUserId(),
                "Cập nhật giao hàng",
                "Đơn hàng " + order.getOrderCode() + " đang ở trạng thái giao hàng " + shipment.getStatus().name() + ".",
                "SHIPMENT",
                "ORDER",
                order.getId(),
                "/orders/" + order.getId()
        );
    }

    private ShipmentResult completeDeliveredShipment(Shipment shipment, Order order, boolean settleCodPaymentOnDelivery) {
        ShipmentStatus oldShipmentStatus = shipment.getStatus();
        OrderStatus oldOrderStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();
        requirePaymentReadyForDelivery(order);
        boolean shipmentStatusChanged = shipment.getStatus() != ShipmentStatus.DELIVERED;
        if (shipmentStatusChanged) {
            shipment.markDelivered();
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            order.markDelivered();
        }

        boolean codPaymentSettled = settleCodPaymentOnDelivery && settleCodPayment(order);
        Order savedOrder = orderRepository.save(order);
        Shipment savedShipment = shipmentRepository.save(shipment);

        if (shipmentStatusChanged) {
            notificationService.create(newShipmentStatusNotification(savedOrder, savedShipment));
        }
        if (savedOrder.getPaymentMethod() == PaymentMethod.COD && (shipmentStatusChanged || codPaymentSettled)) {
            digitalLibraryService.grantPurchasedAccessForOrder(savedOrder);
        }
        if (shipmentStatusChanged) {
            orderTimelineService.recordShipmentStatusChanged(savedOrder, savedShipment, oldShipmentStatus, savedShipment.getStatus());
        }
        if (oldOrderStatus != savedOrder.getStatus()) {
            orderTimelineService.recordStatusChanged(savedOrder, oldOrderStatus, savedOrder.getStatus());
        }
        if (oldPaymentStatus != savedOrder.getPaymentStatus()
                && savedOrder.getPaymentStatus() == PaymentStatus.PAID) {
            paymentRepository.findByOrderId(savedOrder.getId())
                    .ifPresent(payment -> orderTimelineService.recordPaymentPaid(savedOrder, payment));
        }

        return shipmentAssembler.toResult(savedShipment, savedOrder);
    }

    private void requirePaymentReadyForDelivery(Order order) {
        if (order.getPaymentMethod() == PaymentMethod.BANK_TRANSFER_QR
                && order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ApplicationException(ApplicationErrorCode.ORDER_PAYMENT_NOT_PAID);
        }
    }

    private boolean settleCodPayment(Order order) {
        if (order.getPaymentMethod() != PaymentMethod.COD) {
            return false;
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PAYMENT_NOT_FOUND));
        boolean paymentChanged = payment.getStatus() != PaymentStatus.PAID;
        Instant settledAt = paymentChanged ? Instant.now() : payment.getPaidAt();
        if (settledAt == null) {
            settledAt = Instant.now();
        }

        if (paymentChanged) {
            payment.markPaid(
                    payment.getMerchantId(),
                    null,
                    payment.getReferenceCode(),
                    null,
                    settledAt
            );
            paymentRepository.save(payment);
        }
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            order.markPaymentPaid(settledAt);
            return true;
        }

        return paymentChanged;
    }
}

