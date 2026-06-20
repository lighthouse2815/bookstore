package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.AssignShipmentCommand;
import com.bookstore.bookstore.application.command.UpdateShipmentStatusCommand;
import com.bookstore.bookstore.application.result.ShipmentResult;
import com.bookstore.bookstore.presentation.request.AssignShipmentRequest;
import com.bookstore.bookstore.presentation.request.UpdateShipmentStatusRequest;
import com.bookstore.bookstore.presentation.response.ShipmentResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ShipmentWebMapper {

    public AssignShipmentCommand toAssignCommand(AssignShipmentRequest request) {
        return new AssignShipmentCommand(request.orderId(), request.shipperId());
    }

    public UpdateShipmentStatusCommand toUpdateStatusCommand(
            UUID shipmentId,
            UUID shipperId,
            UpdateShipmentStatusRequest request
    ) {
        return new UpdateShipmentStatusCommand(
                shipmentId,
                shipperId,
                request.status(),
                request.failureReason()
        );
    }

    public ShipmentResponse toResponse(ShipmentResult result) {
        return new ShipmentResponse(
                result.shipmentId(),
                result.orderId(),
                result.orderCode(),
                result.shipperId(),
                result.paymentMethod(),
                result.paymentStatus(),
                result.orderStatus(),
                result.shipmentStatus(),
                result.totalAmount(),
                result.finalAmount(),
                result.receiverName(),
                result.receiverPhone(),
                result.receiverAddress(),
                result.failureReason(),
                result.assignedAt(),
                result.updatedAt(),
                result.pickedUpAt(),
                result.deliveringAt(),
                result.deliveredAt(),
                result.failedAt()
        );
    }
}
