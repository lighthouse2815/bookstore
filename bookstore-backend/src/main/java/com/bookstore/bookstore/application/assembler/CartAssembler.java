package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.result.CartItemResult;
import com.bookstore.bookstore.application.result.CartResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.CartItem;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartAssembler {

    private final IBookRepository bookRepository;
    private final IDigitalAssetRepository digitalAssetRepository;

    public CartResult emptyResult(UUID userId) {
        return new CartResult(
                null,
                userId,
                List.of(),
                0,
                BigDecimal.ZERO
        );
    }

    public CartResult toResult(Cart cart) {
        if (cart.getItems().isEmpty()) {
            return new CartResult(
                    cart.getId(),
                    cart.getUserId(),
                    List.of(),
                    0,
                    BigDecimal.ZERO
            );
        }

        Map<UUID, DigitalAsset> digitalAssetsById = digitalAssetRepository.findAllByIdsActive(
                cart.getItems().stream()
                        .map(CartItem::getDigitalAssetId)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList()
        ).stream().collect(Collectors.toMap(DigitalAsset::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeleted(
                cart.getItems().stream()
                        .map(item -> resolveBookId(item, digitalAssetsById))
                        .distinct()
                        .toList()
        ).stream().collect(Collectors.toMap(Book::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<CartItemResult> items = cart.getItems().stream()
                .map(item -> toItemResult(item, booksById, digitalAssetsById))
                .toList();

        int totalQuantity = items.stream()
                .mapToInt(CartItemResult::quantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResult::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResult(
                cart.getId(),
                cart.getUserId(),
                items,
                totalQuantity,
                totalAmount
        );
    }

    private CartItemResult toItemResult(
            CartItem item,
            Map<UUID, Book> booksById,
            Map<UUID, DigitalAsset> digitalAssetsById
    ) {
        if (item.isDigitalAsset()) {
            DigitalAsset digitalAsset = digitalAssetsById.get(item.getDigitalAssetId());
            if (digitalAsset == null) {
                throw new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND);
            }

            Book book = booksById.get(digitalAsset.getBookId());
            if (book == null) {
                throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
            }

            BigDecimal lineTotal = digitalAsset.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartItemResult(
                    item.getId(),
                    item.getItemType(),
                    book.getId(),
                    digitalAsset.getId(),
                    book.getTitle(),
                    digitalAsset.getTitle(),
                    digitalAsset.getFormat(),
                    book.getPrimaryImageUrl(),
                    digitalAsset.getPrice(),
                    item.getQuantity(),
                    lineTotal
            );
        }

        Book book = booksById.get(item.getBookId());
        if (book == null) {
            throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
        }

        BigDecimal lineTotal = book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResult(
                item.getId(),
                item.getItemType(),
                item.getBookId(),
                null,
                book.getTitle(),
                null,
                null,
                book.getPrimaryImageUrl(),
                book.getPrice(),
                item.getQuantity(),
                lineTotal
        );
    }

    private UUID resolveBookId(CartItem item, Map<UUID, DigitalAsset> digitalAssetsById) {
        if (item.getBookId() != null) {
            return item.getBookId();
        }

        DigitalAsset digitalAsset = digitalAssetsById.get(item.getDigitalAssetId());
        if (digitalAsset == null) {
            throw new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND);
        }
        return digitalAsset.getBookId();
    }
}
