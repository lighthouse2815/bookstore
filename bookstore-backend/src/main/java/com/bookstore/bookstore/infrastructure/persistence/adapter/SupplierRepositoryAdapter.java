package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ISupplierRepository;
import com.bookstore.bookstore.domain.model.Supplier;
import com.bookstore.bookstore.infrastructure.persistence.entity.SupplierJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.SupplierPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.SupplierJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SupplierRepositoryAdapter implements ISupplierRepository {

    private final SupplierJpaRepository supplierJpaRepository;
    private final SupplierPersistenceMapper supplierPersistenceMapper;

    @Override
    public List<Supplier> findAllActive() {
        return supplierJpaRepository.findAllActive().stream()
                .map(supplierPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Supplier> findByIdActive(UUID supplierId) {
        return supplierJpaRepository.findByIdActive(supplierId)
                .map(supplierPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Supplier> findByIdIncludingDeleted(UUID supplierId) {
        return supplierJpaRepository.findByIdIncludingDeleted(supplierId)
                .map(supplierPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByNameIncludingDeleted(String name) {
        return supplierJpaRepository.existsByNameIncludingDeleted(name);
    }

    @Override
    public Supplier save(Supplier supplier) {
        SupplierJpaEntity entity = supplierJpaRepository.findByIdIncludingDeleted(supplier.getId())
                .orElseGet(SupplierJpaEntity::new);
        supplierPersistenceMapper.copyToEntity(entity, supplier);
        return supplierPersistenceMapper.toDomain(supplierJpaRepository.save(entity));
    }
}
