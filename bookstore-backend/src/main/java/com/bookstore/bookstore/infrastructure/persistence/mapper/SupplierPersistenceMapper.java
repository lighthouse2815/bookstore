package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Supplier;
import com.bookstore.bookstore.infrastructure.persistence.entity.SupplierJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SupplierPersistenceMapper {

    public Supplier toDomain(SupplierJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Supplier(
                entity.getId(),
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getNote(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(SupplierJpaEntity entity, Supplier supplier) {
        entity.setId(supplier.getId());
        entity.setName(supplier.getName());
        entity.setPhone(supplier.getPhone());
        entity.setEmail(supplier.getEmail());
        entity.setAddress(supplier.getAddress());
        entity.setNote(supplier.getNote());
        entity.setCreatedAt(supplier.getCreatedAt());
        entity.setUpdatedAt(supplier.getUpdatedAt());
        entity.setDeletedAt(supplier.getDeletedAt());
    }
}
