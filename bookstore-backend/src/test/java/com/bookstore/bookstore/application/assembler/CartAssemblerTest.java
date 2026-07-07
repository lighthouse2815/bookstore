package com.bookstore.bookstore.application.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.result.CartResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.domain.model.Cart;
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
class CartAssemblerTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IDigitalAssetRepository digitalAssetRepository;

    @InjectMocks
    private CartAssembler cartAssembler;

    @Test
    void emptyResult_returnsEmptyCartResult() {
        UUID userId = UUID.randomUUID();

        CartResult result = cartAssembler.emptyResult(userId);

        assertEquals(null, result.cartId());
        assertEquals(userId, result.userId());
        assertEquals(List.of(), result.items());
        assertEquals(0, result.totalQuantity());
        assertEquals(BigDecimal.ZERO, result.totalAmount());
    }

    @Test
    void toResult_withEmptyCart_returnsZeroTotalsWithoutLoadingBooks() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.EPOCH;
        Cart cart = new Cart(
                UUID.randomUUID(),
                userId,
                List.of(),
                now,
                now
        );

        CartResult result = cartAssembler.toResult(cart);

        assertEquals(cart.getId(), result.cartId());
        assertEquals(userId, result.userId());
        assertEquals(List.of(), result.items());
        assertEquals(0, result.totalQuantity());
        assertEquals(BigDecimal.ZERO, result.totalAmount());
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(digitalAssetRepository);
    }

    @Test
    void toResult_withItems_returnsDetailedResult() {
        UUID userId = UUID.randomUUID();
        Book book = book();
        Instant now = Instant.EPOCH;
        Cart cart = new Cart(
                UUID.randomUUID(),
                userId,
                List.of(),
                now,
                now
        );
        cart.addItem(book.getId(), 2, book.getStockQuantity());

        when(bookRepository.findAllByIdsIncludingDeleted(List.of(book.getId()))).thenReturn(List.of(book));
        when(digitalAssetRepository.findAllByIdsActive(List.of())).thenReturn(List.of());

        CartResult result = cartAssembler.toResult(cart);

        assertEquals(cart.getId(), result.cartId());
        assertEquals(userId, result.userId());
        assertEquals(1, result.items().size());
        assertEquals(cart.getItems().get(0).getId(), result.items().get(0).id());
        assertEquals(book.getId(), result.items().get(0).bookId());
        assertEquals(book.getTitle(), result.items().get(0).bookTitle());
        assertEquals(book.getImages().get(0).getImageUrl(), result.items().get(0).imageUrl());
        assertEquals(book.getPrice(), result.items().get(0).price());
        assertEquals(2, result.items().get(0).quantity());
        assertEquals(new BigDecimal("20.00"), result.items().get(0).lineTotal());
        assertEquals(2, result.totalQuantity());
        assertEquals(new BigDecimal("20.00"), result.totalAmount());
    }

    @Test
    void toResult_whenBookDataMissing_rejectsBookNotFound() {
        UUID userId = UUID.randomUUID();
        Book book = book();
        Instant now = Instant.EPOCH;
        Cart cart = new Cart(
                UUID.randomUUID(),
                userId,
                List.of(),
                now,
                now
        );
        cart.addItem(book.getId(), 1, book.getStockQuantity());

        when(bookRepository.findAllByIdsIncludingDeleted(List.of(book.getId()))).thenReturn(List.of());
        when(digitalAssetRepository.findAllByIdsActive(List.of())).thenReturn(List.of());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> cartAssembler.toResult(cart)
        );

        assertEquals(ApplicationErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
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
