package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ShipmentJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"order", "shipper"})
    List<ShipmentJpaEntity> findAll();

    @EntityGraph(attributePaths = {"order", "shipper"})
    Page<ShipmentJpaEntity> findAllByOrderByAssignedAtDesc(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"order", "shipper"})
    Optional<ShipmentJpaEntity> findById(UUID shipmentId);

    @EntityGraph(attributePaths = {"order", "shipper"})
    Optional<ShipmentJpaEntity> findByIdAndShipperId(UUID shipmentId, UUID shipperId);

    @EntityGraph(attributePaths = {"order", "shipper"})
    List<ShipmentJpaEntity> findAllByShipperIdOrderByAssignedAtDesc(UUID shipperId);

    @EntityGraph(attributePaths = {"order", "shipper"})
    Page<ShipmentJpaEntity> findAllByShipperIdOrderByAssignedAtDesc(UUID shipperId, Pageable pageable);

    @EntityGraph(attributePaths = {"order", "shipper"})
    List<ShipmentJpaEntity> findAllByOrderIdOrderByAssignedAtDesc(UUID orderId);
}
