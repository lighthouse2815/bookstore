package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.AddCartItemCommand;
import com.bookstore.bookstore.application.command.RemoveCartItemCommand;
import com.bookstore.bookstore.application.command.UpdateCartItemCommand;
import com.bookstore.bookstore.application.result.CartItemResult;
import com.bookstore.bookstore.application.result.CartResult;
import com.bookstore.bookstore.presentation.request.AddCartItemRequest;
import com.bookstore.bookstore.presentation.request.UpdateCartItemRequest;
import com.bookstore.bookstore.presentation.response.CartItemResponse;
import com.bookstore.bookstore.presentation.response.CartResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartWebMapper {

    public AddCartItemCommand toAddCommand(UUID userId, AddCartItemRequest request) {
        return new AddCartItemCommand(
                userId,
                request.bookId(),
                request.quantity()
        );
    }

    public UpdateCartItemCommand toUpdateCommand(UUID userId, UUID bookId, UpdateCartItemRequest request) {
        return new UpdateCartItemCommand(
                userId,
                bookId,
                request.quantity()
        );
    }

    public RemoveCartItemCommand toRemoveCommand(UUID userId, UUID bookId) {
        return new RemoveCartItemCommand(userId, bookId);
    }

    public CartResponse toResponse(CartResult result) {
        return new CartResponse(
                result.cartId(),
                result.userId(),
                result.items().stream()
                        .map(this::toItemResponse)
                        .toList(),
                result.totalQuantity(),
                result.totalAmount()
        );
    }

    private CartItemResponse toItemResponse(CartItemResult result) {
        return new CartItemResponse(
                result.bookId(),
                result.bookTitle(),
                result.imageUrl(),
                result.price(),
                result.quantity(),
                result.lineTotal()
        );
    }
}
