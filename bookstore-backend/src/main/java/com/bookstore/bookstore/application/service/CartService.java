package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.CartAssembler;
import com.bookstore.bookstore.application.command.AddCartItemCommand;
import com.bookstore.bookstore.application.command.RemoveCartItemCommand;
import com.bookstore.bookstore.application.command.UpdateCartItemCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.ICartService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.application.result.CartResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final ICartRepository cartRepository;
    private final IBookRepository bookRepository;
    private final CartAssembler cartAssembler;

    @Override
    @Transactional(readOnly = true)
    public CartResult getMyCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .map(cartAssembler::toResult)
                .orElseGet(() -> cartAssembler.emptyResult(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartResult addItem(AddCartItemCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID userId = command.userId();
        UUID bookId = command.bookId();

        Book book = bookRepository.findByIdActive(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Instant now = Instant.now();
                    return new Cart(
                            UUID.randomUUID(),
                            userId,
                            List.of(),
                            now,
                            now
                    );
                });

        cart.addItem(book.getId(), command.quantity(), book.getStockQuantity());

        return cartAssembler.toResult(cartRepository.save(cart));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartResult updateItem(UpdateCartItemCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID userId = command.userId();
        UUID bookId = command.bookId();

        Book book = bookRepository.findByIdActive(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CART_NOT_FOUND));

        cart.updateItem(book.getId(), command.quantity(), book.getStockQuantity());

        return cartAssembler.toResult(cartRepository.save(cart));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeItem(RemoveCartItemCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID userId = command.userId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CART_NOT_FOUND));

        cart.removeItem(command.bookId());
        cartRepository.save(cart);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear(UUID userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }
}
