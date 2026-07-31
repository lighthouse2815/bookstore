package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateSupplierCommand;
import com.bookstore.bookstore.application.command.DeleteSupplierCommand;
import com.bookstore.bookstore.application.command.UpdateSupplierCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.ISupplierService;
import com.bookstore.bookstore.application.port.out.ISupplierRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.domain.model.Supplier;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierService implements ISupplierService {

    private final ISupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getAll() {
        return supplierRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<Supplier> getAll(PageQuery pageQuery) {
        int page = pageQuery.page();
        int size = pageQuery.size();
        return supplierRepository.findPageActive(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getById(UUID supplierId) {
        if (supplierId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "supplierId");
        }

        return supplierRepository.findByIdActive(supplierId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SUPPLIER_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Supplier create(CreateSupplierCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String name = StringUtils.trimToNull(command.name());
        String phone = StringUtils.trimToNull(command.phone());
        String email = StringUtils.trimToNull(command.email());
        String address = StringUtils.trimToNull(command.address());
        String note = StringUtils.trimToNull(command.note());

        if (supplierRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.SUPPLIER_NAME_ALREADY_EXISTS);
        }

        Instant now = Instant.now();
        Supplier supplier = new Supplier(
                UUID.randomUUID(),
                name,
                phone,
                email,
                address,
                note,
                now,
                now,
                null
        );

        return supplierRepository.save(supplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Supplier update(UpdateSupplierCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Supplier currentSupplier = supplierRepository.findByIdActive(command.supplierId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SUPPLIER_NOT_FOUND));

        String name = StringUtils.trimToNull(command.name());
        String phone = StringUtils.trimToNull(command.phone());
        String email = StringUtils.trimToNull(command.email());
        String address = StringUtils.trimToNull(command.address());
        String note = StringUtils.trimToNull(command.note());

        if (!currentSupplier.getName().equals(name) && supplierRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.SUPPLIER_NAME_ALREADY_EXISTS);
        }

        currentSupplier.updateSupplier(name, phone, email, address, note);
        return supplierRepository.save(currentSupplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteSupplierCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Supplier currentSupplier = supplierRepository.findByIdActive(command.supplierId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SUPPLIER_NOT_FOUND));

        currentSupplier.softDelete();
        supplierRepository.save(currentSupplier);
    }

}
