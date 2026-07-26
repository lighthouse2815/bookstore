package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Supplier;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ISupplierRepository {

    List<Supplier> findAllActive();

    PageSliceResult<Supplier> findPageActive(int page, int size);

    Optional<Supplier> findByIdActive(UUID supplierId);

    Optional<Supplier> findByIdIncludingDeleted(UUID supplierId);

    boolean existsByIdIncludingDeleted(UUID supplierId);

    boolean existsByNameIncludingDeleted(String name);

    Supplier save(Supplier supplier);
}
