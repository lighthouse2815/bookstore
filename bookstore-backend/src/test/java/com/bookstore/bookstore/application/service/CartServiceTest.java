package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.CartAssembler;
import com.bookstore.bookstore.application.command.AddCartItemCommand;
import com.bookstore.bookstore.application.command.RemoveCartItemCommand;
import com.bookstore.bookstore.application.command.UpdateCartItemCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.result.CartItemResult;
import com.bookstore.bookstore.application.result.CartResult;
import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.FileAsset;
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
class CartServiceTest {

    @Mock
    private ICartRepository cartRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IDigitalAssetRepository digitalAssetRepository;

    @Mock
    private CartAssembler cartAssembler;

    @InjectMocks
    private CartService cartService;

    @Test
    void getMyCart_withoutCart_returnsEmptyResult() {
        UUID userId = UUID.randomUUID();
        CartResult expected = new CartResult(null, userId, List.of(), 0, BigDecimal.ZERO);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartAssembler.emptyResult(userId)).thenReturn(expected);

        CartResult result = cartService.getMyCart(userId);

        assertEquals(expected, result);
    }

    @Test
    void addPhysicalItem_createsCartAndReturnsDetailedResult() {
        UUID userId = UUID.randomUUID();
        Book book = book(new BigDecimal("10.00"), 10);
        AddCartItemCommand command = new AddCartItemCommand(userId, book.getId(), 2);
        CartResult expected = new CartResult(
                UUID.randomUUID(),
                userId,
                List.of(new CartItemResult(
                        UUID.randomUUID(),
                        PurchaseItemType.PHYSICAL_BOOK,
                        book.getId(),
                        null,
                        book.getTitle(),
                        null,
                        null,
                        null,
                        book.getPrice(),
                        2,
                        new BigDecimal("20.00")
                )),
                2,
                new BigDecimal("20.00")
        );

        when(bookRepository.findByIdActive(book.getId())).thenReturn(Optional.of(book));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartAssembler.toResult(org.mockito.ArgumentMatchers.any(Cart.class))).thenReturn(expected);

        CartResult result = cartService.addItem(command);

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getItems().size());
        assertEquals(PurchaseItemType.PHYSICAL_BOOK, captor.getValue().getItems().get(0).getItemType());
        assertEquals(book.getId(), captor.getValue().getItems().get(0).getBookId());
        assertEquals(2, captor.getValue().getItems().get(0).getQuantity());
        assertEquals(expected, result);
    }

    @Test
    void addDigitalItem_addsSingleAccessItem() {
        UUID userId = UUID.randomUUID();
        UUID digitalAssetId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        DigitalAsset digitalAsset = digitalAsset(bookId, digitalAssetId, new BigDecimal("5.00"), true, true, true);
        CartResult expected = new CartResult(
                UUID.randomUUID(),
                userId,
                List.of(new CartItemResult(
                        UUID.randomUUID(),
                        PurchaseItemType.DIGITAL_ASSET,
                        bookId,
                        digitalAssetId,
                        "Book Title",
                        digitalAsset.getTitle(),
                        digitalAsset.getFormat(),
                        null,
                        digitalAsset.getPrice(),
                        1,
                        digitalAsset.getPrice()
                )),
                1,
                digitalAsset.getPrice()
        );

        when(digitalAssetRepository.findByIdActive(digitalAssetId)).thenReturn(Optional.of(digitalAsset));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cartAssembler.toResult(org.mockito.ArgumentMatchers.any(Cart.class))).thenReturn(expected);

        CartResult result = cartService.addItem(AddCartItemCommand.digital(userId, digitalAssetId));

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getItems().size());
        assertEquals(PurchaseItemType.DIGITAL_ASSET, captor.getValue().getItems().get(0).getItemType());
        assertEquals(digitalAssetId, captor.getValue().getItems().get(0).getDigitalAssetId());
        assertEquals(1, captor.getValue().getItems().get(0).getQuantity());
        assertEquals(expected, result);
    }

    @Test
    void updateDigitalItem_withQuantityDifferentFromOne_rejectsInvalidArgument() {
        UUID userId = UUID.randomUUID();
        Cart cart = cart(userId);
        UUID digitalAssetId = UUID.randomUUID();
        cart.addDigitalItem(digitalAssetId);
        UUID cartItemId = cart.getItems().get(0).getId();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> cartService.updateItem(new UpdateCartItemCommand(userId, cartItemId, 2))
        );

        assertEquals(ApplicationErrorCode.INVALID_ARGUMENT, exception.getErrorCode());
    }

    @Test
    void removeItem_byCartItemId_savesCartAfterRemovingItem() {
        UUID userId = UUID.randomUUID();
        Book book = book(new BigDecimal("10.00"), 10);
        Cart cart = cart(userId);
        cart.addPhysicalItem(book.getId(), 1, book.getStockQuantity());
        UUID cartItemId = cart.getItems().get(0).getId();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        cartService.removeItem(new RemoveCartItemCommand(userId, cartItemId));

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getItems().size());
    }

    private static Cart cart(UUID userId) {
        Instant now = Instant.EPOCH;
        return new Cart(
                UUID.randomUUID(),
                userId,
                List.of(),
                now,
                now
        );
    }

    private static Book book(BigDecimal price, int stockQuantity) {
        Instant now = Instant.EPOCH;
        return new Book(
                UUID.randomUUID(),
                "Book Title",
                "ISBN-123",
                "Book Description",
                price,
                stockQuantity,
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }

    private static DigitalAsset digitalAsset(
            UUID bookId,
            UUID digitalAssetId,
            BigDecimal price,
            boolean downloadAllowed,
            boolean purchaseAllowed,
            boolean published
    ) {
        Instant now = Instant.EPOCH;
        return new DigitalAsset(
                digitalAssetId,
                bookId,
                DigitalAssetFormat.PDF,
                "Bản PDF",
                fileAsset(FilePurpose.EBOOK_FILE, "ebook.pdf", "private/digital/ebook.pdf"),
                null,
                price,
                downloadAllowed,
                purchaseAllowed,
                published,
                now,
                now,
                null
        );
    }

    private static FileAsset fileAsset(FilePurpose purpose, String originalName, String storageKey) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                UUID.randomUUID(),
                FileProvider.R2,
                purpose,
                "private-bucket",
                storageKey,
                null,
                originalName,
                "application/pdf",
                1_024L,
                "checksum",
                FileVisibility.PRIVATE,
                FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
