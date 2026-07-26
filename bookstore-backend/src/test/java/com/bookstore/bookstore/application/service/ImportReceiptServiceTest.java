package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.ImportReceiptAssembler;
import com.bookstore.bookstore.application.command.CreateImportReceiptCommand;
import com.bookstore.bookstore.application.command.CreateImportReceiptItemCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IImportReceiptRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.port.out.ISupplierRepository;
import com.bookstore.bookstore.application.result.ImportReceiptResult;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.domain.model.Supplier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImportReceiptServiceTest {

    @Mock
    private IImportReceiptRepository importReceiptRepository;

    @Mock
    private ISupplierRepository supplierRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IStockMovementRepository stockMovementRepository;

    @Mock
    private ImportReceiptAssembler importReceiptAssembler;

    @InjectMocks
    private ImportReceiptService importReceiptService;

    @Test
    void create_increasesStockAndRecordsImportMovement() {
        UUID createdBy = UUID.randomUUID();
        Supplier supplier = supplier();
        Book book = book();
        CreateImportReceiptCommand command = new CreateImportReceiptCommand(
                supplier.getId(),
                List.of(new CreateImportReceiptItemCommand(book.getId(), new BigDecimal("12.00"), 5)),
                "Nhap them",
                createdBy
        );
        ImportReceiptResult expected = new ImportReceiptResult(
                UUID.randomUUID(),
                supplier.getId(),
                List.of(),
                new BigDecimal("60.00"),
                "Nhap them",
                Instant.EPOCH,
                Instant.EPOCH,
                createdBy
        );

        when(supplierRepository.existsByIdIncludingDeleted(supplier.getId())).thenReturn(true);
        when(bookRepository.findAllByIdsIncludingDeletedForUpdate(List.of(book.getId()))).thenReturn(List.of(book));
        when(importReceiptRepository.save(any(ImportReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(importReceiptAssembler.toResult(any(ImportReceipt.class))).thenReturn(expected);

        ImportReceiptResult result = importReceiptService.create(command);

        ArgumentCaptor<ImportReceipt> importReceiptCaptor = ArgumentCaptor.forClass(ImportReceipt.class);
        verify(importReceiptRepository).save(importReceiptCaptor.capture());
        assertEquals(new BigDecimal("60.00"), importReceiptCaptor.getValue().getTotalAmount());
        assertEquals(1, importReceiptCaptor.getValue().getItems().size());

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());
        verify(bookRepository).findAllByIdsIncludingDeletedForUpdate(List.of(book.getId()));
        assertEquals(15, bookCaptor.getValue().getStockQuantity());

        ArgumentCaptor<StockMovement> stockMovementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(stockMovementCaptor.capture());
        assertEquals(StockMovementType.IMPORT, stockMovementCaptor.getValue().getType());
        assertEquals(5, stockMovementCaptor.getValue().getQuantity());
        assertEquals(10, stockMovementCaptor.getValue().getBeforeQuantity());
        assertEquals(15, stockMovementCaptor.getValue().getAfterQuantity());

        assertEquals(expected, result);
    }

    @Test
    void getById_whenMissing_rejectsNotFound() {
        UUID receiptId = UUID.randomUUID();
        when(importReceiptRepository.findById(receiptId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> importReceiptService.getById(receiptId)
        );

        assertEquals(ApplicationErrorCode.IMPORT_RECEIPT_NOT_FOUND, exception.getErrorCode());
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

    private static Book book() {
        Instant now = Instant.EPOCH;
        UUID bookId = UUID.randomUUID();
        return new Book(
                bookId,
                "Book Title",
                "ISBN-123",
                "Book Description",
                new BigDecimal("10.00"),
                10,
                List.of(new BookImage(
                        UUID.randomUUID(),
                        bookId,
                        fileAsset(),
                        true,
                        0,
                        null,
                        now
                )),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }

    private static com.bookstore.bookstore.domain.model.FileAsset fileAsset() {
        Instant now = Instant.EPOCH;
        return new com.bookstore.bookstore.domain.model.FileAsset(
                UUID.randomUUID(),
                com.bookstore.bookstore.domain.enums.FileProvider.R2,
                com.bookstore.bookstore.domain.enums.FilePurpose.BOOK_IMAGE,
                "bookstore-assets",
                "public/books/cover.jpg",
                "https://cdn.example.com/public/books/cover.jpg",
                "cover.jpg",
                "image/jpeg",
                1024L,
                "checksum",
                com.bookstore.bookstore.domain.enums.FileVisibility.PUBLIC,
                com.bookstore.bookstore.domain.enums.FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
