package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.ShipmentResult;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.Shipment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentAssembler {

    public ShipmentResult toResult(Shipment shipment, Order order) {
        return new ShipmentResult(
                shipment.getId(),
                order.getId(),
                order.getOrderCode(),
                shipment.getShipperId(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getStatus(),
                shipment.getStatus(),
                order.getTotalAmount(),
                order.getFinalAmount(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                shipment.getFailureReason(),
                shipment.getAssignedAt(),
                shipment.getUpdatedAt(),
                shipment.getPickedUpAt(),
                shipment.getDeliveringAt(),
                shipment.getDeliveredAt(),
                shipment.getFailedAt()
        );
    }
}
