package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.ISupplierRepository;
import com.bookstore.bookstore.domain.model.Supplier;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.infrastructure.persistence.entity.SupplierJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.SupplierPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.SupplierJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Repository
@RequiredArgsConstructor
public class SupplierRepositoryAdapter implements ISupplierRepository {

    private final SupplierJpaRepository supplierJpaRepository;
    private final SupplierPersistenceMapper supplierPersistenceMapper;

    @Override
    public List<Supplier> findAllActive() {
        return supplierJpaRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .map(supplierPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<Supplier> findPageActive(int page, int size) {
        var result = supplierJpaRepository.findAllByDeletedAtIsNull(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return new PageSliceResult<>(
                result.getContent().stream().map(supplierPersistenceMapper::toDomain).toList(),
                result.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public Optional<Supplier> findByIdActive(UUID supplierId) {
        return supplierJpaRepository.findByIdAndDeletedAtIsNull(supplierId)
                .map(supplierPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Supplier> findByIdIncludingDeleted(UUID supplierId) {
        return supplierJpaRepository.findById(supplierId)
                .map(supplierPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID supplierId) {
        return supplierJpaRepository.existsById(supplierId);
    }

    @Override
    public boolean existsByNameIncludingDeleted(String name) {
        return supplierJpaRepository.existsByName(name);
    }

    @Override
    public Supplier save(Supplier supplier) {
        SupplierJpaEntity entity = supplierJpaRepository.findById(supplier.getId())
                .orElseGet(SupplierJpaEntity::new);
        supplierPersistenceMapper.copyToEntity(entity, supplier);
        return supplierPersistenceMapper.toDomain(supplierJpaRepository.save(entity));
    }
}
