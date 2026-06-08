package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CheckoutCommand;
import com.bookstore.bookstore.application.command.UpdateOrderStatusCommand;
import com.bookstore.bookstore.application.result.OrderResult;
import java.util.List;
import java.util.UUID;

public interface IOrderService {

    OrderResult checkout(CheckoutCommand command);

    List<OrderResult> getMyOrders(UUID userId);

    OrderResult getMyOrder(UUID userId, UUID orderId);

    List<OrderResult> getAll();

    OrderResult getById(UUID orderId);

    OrderResult updateStatus(UpdateOrderStatusCommand command);
}
