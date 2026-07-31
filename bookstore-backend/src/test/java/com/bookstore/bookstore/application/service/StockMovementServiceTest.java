package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.StockMovementAssembler;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.result.StockMovementResult;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.domain.model.StockMovement;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private IStockMovementRepository stockMovementRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private StockMovementAssembler stockMovementAssembler;

    @InjectMocks
    private StockMovementService stockMovementService;

    @Test
    void getByBookId_returnsMappedResults() {
        Book book = book();
        StockMovement stockMovement = stockMovement(book.getId());
        StockMovementResult expected = new StockMovementResult(
                stockMovement.getId(),
                stockMovement.getBookId(),
                stockMovement.getType(),
                stockMovement.getQuantity(),
                stockMovement.getBeforeQuantity(),
                stockMovement.getAfterQuantity(),
                stockMovement.getReferenceId(),
                stockMovement.getReferenceType(),
                stockMovement.getNote(),
                stockMovement.getCreatedAt(),
                stockMovement.getCreatedBy()
        );

        when(bookRepository.existsByIdIncludingDeleted(book.getId())).thenReturn(true);
        when(stockMovementRepository.findAllByBookId(book.getId())).thenReturn(List.of(stockMovement));
        when(stockMovementAssembler.toResult(stockMovement)).thenReturn(expected);

        List<StockMovementResult> results = stockMovementService.getByBookId(book.getId());

        assertEquals(List.of(expected), results);
    }

    @Test
    void getByBookId_whenBookMissing_rejectsBookNotFound() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.existsByIdIncludingDeleted(bookId)).thenReturn(false);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> stockMovementService.getByBookId(bookId)
        );

        assertEquals(ApplicationErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
    }

    private static StockMovement stockMovement(UUID bookId) {
        return new StockMovement(
                UUID.randomUUID(),
                bookId,
                StockMovementType.SALE,
                2,
                10,
                8,
                UUID.randomUUID(),
                "ORDER",
                null,
                Instant.EPOCH,
                UUID.randomUUID()
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
