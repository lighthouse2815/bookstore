package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.OrderTimelineEventResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.domain.model.Refund;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.bookstore.bookstore.domain.model.Shipment;
import java.util.List;
import java.util.UUID;

public interface IOrderTimelineService {

    List<OrderTimelineEventResult> getMyTimeline(UUID userId, UUID orderId);

    List<OrderTimelineEventResult> getOrderTimeline(UUID orderId);

    void recordOrderCreated(Order order);

    void recordCouponsApplied(Order order);

    void recordPaymentPending(Order order, Payment payment);

    void recordPaymentPaid(Order order, Payment payment);

    void recordStatusChanged(Order order, OrderStatus oldStatus, OrderStatus newStatus);

    void recordShipmentAssigned(Order order, Shipment shipment, String shipperName);

    void recordShipmentStatusChanged(Order order, Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus);

    void recordOrderCancelled(Order order, String reason);

    void recordStockRolledBack(Order order);

    void recordCouponRolledBack(Order order, String couponCode);

    void recordReturnRequested(Order order, ReturnRequest returnRequest);

    void recordReturnApproved(Order order, ReturnRequest returnRequest);

    void recordReturnRejected(Order order, ReturnRequest returnRequest);

    void recordReturnCancelled(Order order, ReturnRequest returnRequest);

    void recordRefundInternalApproved(Order order, ReturnRequest returnRequest);

    void recordRefundStateChanged(Order order, Refund refund, com.bookstore.bookstore.domain.enums.RefundStatus previousStatus);

    void recordStockRestockedFromReturn(Order order, ReturnRequest returnRequest, int totalQuantity);
}
