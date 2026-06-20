package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.AssignShipmentCommand;
import com.bookstore.bookstore.application.command.UpdateShipmentStatusCommand;
import com.bookstore.bookstore.application.result.ShipmentResult;
import java.util.List;
import java.util.UUID;

public interface IShipmentService {

    ShipmentResult assign(AssignShipmentCommand command);

    List<ShipmentResult> getAll();

    ShipmentResult getById(UUID shipmentId);

    List<ShipmentResult> getMyShipments(UUID shipperId);

    ShipmentResult getMyShipment(UUID shipperId, UUID shipmentId);

    ShipmentResult updateMyShipmentStatus(UpdateShipmentStatusCommand command);

    ShipmentResult confirmDeliveredByAdmin(UUID shipmentId);
}
