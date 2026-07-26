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
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.result.CartResult;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.CartItem;
import com.bookstore.bookstore.domain.model.DigitalAsset;
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
    private final IDigitalAssetRepository digitalAssetRepository;
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

        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseGet(() -> createEmptyCart(command.userId()));

        if (command.itemType() == PurchaseItemType.DIGITAL_ASSET) {
            DigitalAsset digitalAsset = requirePurchasableDigitalAsset(command.digitalAssetId());
            cart.addDigitalItem(digitalAsset.getId());
            return cartAssembler.toResult(cartRepository.save(cart));
        }

        Book book = bookRepository.findByIdActive(command.bookId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        cart.addPhysicalItem(book.getId(), command.quantity(), book.getStockQuantity());
        return cartAssembler.toResult(cartRepository.save(cart));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartResult updateItem(UpdateCartItemCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CART_NOT_FOUND));
        CartItem cartItem = resolveCartItem(cart, command.itemReferenceId());

        if (cartItem.isDigitalAsset()) {
            if (command.quantity() != 1) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "quantity");
            }
            return cartAssembler.toResult(cart);
        }

        Book book = bookRepository.findByIdActive(cartItem.getBookId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        cart.updatePhysicalItem(cartItem.getId(), command.quantity(), book.getStockQuantity());
        return cartAssembler.toResult(cartRepository.save(cart));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeItem(RemoveCartItemCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CART_NOT_FOUND));

        CartItem cartItem = resolveCartItem(cart, command.itemReferenceId());
        cart.removeItemById(cartItem.getId());
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

    private Cart createEmptyCart(UUID userId) {
        Instant now = Instant.now();
        return new Cart(
                UUID.randomUUID(),
                userId,
                List.of(),
                now,
                now
        );
    }

    private DigitalAsset requirePurchasableDigitalAsset(UUID digitalAssetId) {
        DigitalAsset digitalAsset = digitalAssetRepository.findByIdActive(digitalAssetId)
                .filter(DigitalAsset::isPublished)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND));

        if (!digitalAsset.isPurchaseAllowed()) {
            throw new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_PURCHASE_NOT_ALLOWED);
        }
        return digitalAsset;
    }

    private CartItem resolveCartItem(Cart cart, UUID itemReferenceId) {
        CartItem cartItem = cart.findItemById(itemReferenceId);
        if (cartItem != null) {
            return cartItem;
        }

        cartItem = cart.findPhysicalItemByBookId(itemReferenceId);
        if (cartItem != null) {
            return cartItem;
        }

        cartItem = cart.findDigitalItemByDigitalAssetId(itemReferenceId);
        if (cartItem != null) {
            return cartItem;
        }

        throw new ApplicationException(ApplicationErrorCode.CART_ITEM_NOT_FOUND);
    }
}
