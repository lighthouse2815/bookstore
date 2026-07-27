package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateSupplierCommand;
import com.bookstore.bookstore.application.command.DeleteSupplierCommand;
import com.bookstore.bookstore.application.command.UpdateSupplierCommand;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.domain.model.Supplier;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.UUID;

public interface ISupplierService {

    List<Supplier> getAll();

    PageSliceResult<Supplier> getAll(PageQuery pageQuery);

    Supplier getById(UUID supplierId);

    Supplier create(CreateSupplierCommand command);

    Supplier update(UpdateSupplierCommand command);

    void delete(DeleteSupplierCommand command);
}
