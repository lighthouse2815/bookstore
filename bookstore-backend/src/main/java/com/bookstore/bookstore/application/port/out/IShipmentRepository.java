package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Shipment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IShipmentRepository {

    List<Shipment> findAll();

    Optional<Shipment> findById(UUID shipmentId);

    Optional<Shipment> findByIdAndShipperId(UUID shipmentId, UUID shipperId);

    List<Shipment> findAllByShipperId(UUID shipperId);

    List<Shipment> findAllByOrderId(UUID orderId);

    Shipment save(Shipment shipment);
}
