package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateSupplierCommand;
import com.bookstore.bookstore.application.command.DeleteSupplierCommand;
import com.bookstore.bookstore.application.command.UpdateSupplierCommand;
import com.bookstore.bookstore.domain.model.Supplier;
import com.bookstore.bookstore.presentation.request.CreateSupplierRequest;
import com.bookstore.bookstore.presentation.request.UpdateSupplierRequest;
import com.bookstore.bookstore.presentation.response.SupplierResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SupplierWebMapper {

    public CreateSupplierCommand toCreateCommand(CreateSupplierRequest request) {
        return new CreateSupplierCommand(
                request.name(),
                request.phone(),
                request.email(),
                request.address(),
                request.note()
        );
    }

    public UpdateSupplierCommand toUpdateCommand(UUID supplierId, UpdateSupplierRequest request) {
        return new UpdateSupplierCommand(
                supplierId,
                request.name(),
                request.phone(),
                request.email(),
                request.address(),
                request.note()
        );
    }

    public DeleteSupplierCommand toDeleteCommand(UUID supplierId) {
        return new DeleteSupplierCommand(supplierId);
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getNote(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}
