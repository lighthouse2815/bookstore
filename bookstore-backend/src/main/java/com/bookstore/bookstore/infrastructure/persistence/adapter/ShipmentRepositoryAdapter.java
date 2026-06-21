package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IShipmentRepository;
import com.bookstore.bookstore.domain.model.Shipment;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ShipmentJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ShipmentPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.ShipmentJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShipmentRepositoryAdapter implements IShipmentRepository {

    private final ShipmentJpaRepository shipmentJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ShipmentPersistenceMapper shipmentPersistenceMapper;

    @Override
    public List<Shipment> findAll() {
        return shipmentJpaRepository.findAll().stream()
                .map(shipmentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Shipment> findById(UUID shipmentId) {
        return shipmentJpaRepository.findById(shipmentId)
                .map(shipmentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByIdAndShipperId(UUID shipmentId, UUID shipperId) {
        return shipmentJpaRepository.findByIdAndShipperId(shipmentId, shipperId)
                .map(shipmentPersistenceMapper::toDomain);
    }

    @Override
    public List<Shipment> findAllByShipperId(UUID shipperId) {
        return shipmentJpaRepository.findAllByShipperIdOrderByAssignedAtDesc(shipperId).stream()
                .map(shipmentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Shipment> findAllByOrderId(UUID orderId) {
        return shipmentJpaRepository.findAllByOrderIdOrderByAssignedAtDesc(orderId).stream()
                .map(shipmentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentJpaEntity entity = shipmentJpaRepository.findById(shipment.getId())
                .orElseGet(ShipmentJpaEntity::new);
        OrderJpaEntity order = orderJpaRepository.getReferenceById(shipment.getOrderId());
        UserJpaEntity shipper = userJpaRepository.getReferenceById(shipment.getShipperId());
        shipmentPersistenceMapper.copyToEntity(shipment, entity, order, shipper);
        return shipmentPersistenceMapper.toDomain(shipmentJpaRepository.save(entity));
    }
}
