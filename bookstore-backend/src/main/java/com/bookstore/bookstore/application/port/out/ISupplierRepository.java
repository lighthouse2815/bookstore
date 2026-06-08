package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Supplier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ISupplierRepository {

    List<Supplier> findAllActive();

    Optional<Supplier> findByIdActive(UUID supplierId);

    Optional<Supplier> findByIdIncludingDeleted(UUID supplierId);

    boolean existsByNameIncludingDeleted(String name);

    Supplier save(Supplier supplier);
}
