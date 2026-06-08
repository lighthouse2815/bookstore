package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.AddCartItemCommand;
import com.bookstore.bookstore.application.command.RemoveCartItemCommand;
import com.bookstore.bookstore.application.command.UpdateCartItemCommand;
import com.bookstore.bookstore.application.result.CartResult;
import java.util.UUID;

public interface ICartService {

    CartResult getMyCart(UUID userId);

    CartResult addItem(AddCartItemCommand command);

    CartResult updateItem(UpdateCartItemCommand command);

    void removeItem(RemoveCartItemCommand command);

    void clear(UUID userId);
}
