package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IOrderRepository {

    Optional<Order> findById(UUID orderId);

    List<Order> findByUserId(UUID userId);

    List<Order> findAll();

    Order save(Order order);
}
