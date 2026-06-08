package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.result.CartItemResult;
import com.bookstore.bookstore.application.result.CartResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import java.math.BigDecimal;
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

        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeleted(
                        cart.getItems().stream()
                                .map(item -> item.getBookId())
                                .toList()
                ).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<CartItemResult> items = cart.getItems().stream()
                .map(item -> {
                    Book book = booksById.get(item.getBookId());
                    if (book == null) {
                        throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
                    }

                    BigDecimal lineTotal = book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartItemResult(
                            item.getBookId(),
                            book.getTitle(),
                            book.getImageUrl(),
                            book.getPrice(),
                            item.getQuantity(),
                            lineTotal
                    );
                })
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
}
