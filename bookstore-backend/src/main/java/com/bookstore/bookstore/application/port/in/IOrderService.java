package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateOrderCommand;
import com.bookstore.bookstore.application.command.CancelOrderCommand;
import com.bookstore.bookstore.application.command.CreatePosOrderCommand;
import com.bookstore.bookstore.application.command.UpdateOrderStatusCommand;
import com.bookstore.bookstore.application.result.CreateOrderResult;
import com.bookstore.bookstore.application.result.CreatePosOrderResult;
import com.bookstore.bookstore.application.result.OrderResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.UUID;

public interface IOrderService {

    CreateOrderResult checkout(CreateOrderCommand command);

    CreatePosOrderResult createPosOrder(CreatePosOrderCommand command);

    List<OrderResult> getMyOrders(UUID userId);

    PageSliceResult<OrderResult> getMyOrders(UUID userId, int page, int size);

    OrderResult getMyOrder(UUID userId, UUID orderId);

    List<OrderResult> getAll();

    PageSliceResult<OrderResult> getAll(int page, int size);

    OrderResult getById(UUID orderId);

    OrderResult updateStatus(UpdateOrderStatusCommand command);

    OrderResult cancelMyOrder(CancelOrderCommand command);
}
