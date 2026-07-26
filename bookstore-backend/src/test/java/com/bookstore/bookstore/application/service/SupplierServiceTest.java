package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CreateSupplierCommand;
import com.bookstore.bookstore.application.command.UpdateSupplierCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.ISupplierRepository;
import com.bookstore.bookstore.domain.model.Supplier;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private ISupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void create_savesNormalizedSupplier() {
        when(supplierRepository.existsByNameIncludingDeleted("Supplier A")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Supplier result = supplierService.create(new CreateSupplierCommand(
                " Supplier A ",
                "0123456789",
                "supplier@example.com",
                " Address ",
                " Note "
        ));

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(captor.capture());
        assertEquals("Supplier A", captor.getValue().getName());
        assertEquals("0123456789", captor.getValue().getPhone());
        assertEquals("supplier@example.com", captor.getValue().getEmail());
        assertEquals("Address", captor.getValue().getAddress());
        assertEquals("Note", captor.getValue().getNote());
        assertEquals("Supplier A", result.getName());
    }

    @Test
    void create_whenNameExists_rejectsConflict() {
        when(supplierRepository.existsByNameIncludingDeleted("Supplier A")).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> supplierService.create(new CreateSupplierCommand(
                        "Supplier A",
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.SUPPLIER_NAME_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void update_whenRenamingToExistingName_rejectsConflict() {
        Supplier supplier = supplier();
        when(supplierRepository.findByIdActive(supplier.getId())).thenReturn(Optional.of(supplier));
        when(supplierRepository.existsByNameIncludingDeleted("Supplier B")).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> supplierService.update(new UpdateSupplierCommand(
                        supplier.getId(),
                        "Supplier B",
                        supplier.getPhone(),
                        supplier.getEmail(),
                        supplier.getAddress(),
                        supplier.getNote()
                ))
        );

        assertEquals(ApplicationErrorCode.SUPPLIER_NAME_ALREADY_EXISTS, exception.getErrorCode());
    }

    private static Supplier supplier() {
        Instant now = Instant.EPOCH;
        return new Supplier(
                UUID.randomUUID(),
                "Supplier A",
                "0123456789",
                "supplier@example.com",
                "Address",
                "Note",
                now,
                now,
                null
        );
    }
}
