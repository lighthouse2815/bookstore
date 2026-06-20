package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Shipment;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ShipmentJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ShipmentPersistenceMapper {

    public Shipment toDomain(ShipmentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Shipment(
                entity.getId(),
                entity.getOrder().getId(),
                entity.getShipper().getId(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getAssignedAt(),
                entity.getUpdatedAt(),
                entity.getPickedUpAt(),
                entity.getDeliveringAt(),
                entity.getDeliveredAt(),
                entity.getFailedAt()
        );
    }

    public void copyToEntity(
            Shipment shipment,
            ShipmentJpaEntity entity,
            OrderJpaEntity order,
            UserJpaEntity shipper
    ) {
        entity.setId(shipment.getId());
        entity.setOrder(order);
        entity.setShipper(shipper);
        entity.setStatus(shipment.getStatus());
        entity.setFailureReason(shipment.getFailureReason());
        entity.setAssignedAt(shipment.getAssignedAt());
        entity.setUpdatedAt(shipment.getUpdatedAt());
        entity.setPickedUpAt(shipment.getPickedUpAt());
        entity.setDeliveringAt(shipment.getDeliveringAt());
        entity.setDeliveredAt(shipment.getDeliveredAt());
        entity.setFailedAt(shipment.getFailedAt());
    }
}
