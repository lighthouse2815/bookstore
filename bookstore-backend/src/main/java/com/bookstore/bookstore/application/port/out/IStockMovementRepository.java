package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.StockMovement;
import java.util.List;
import java.util.UUID;

public interface IStockMovementRepository {

    List<StockMovement> findAll();

    List<StockMovement> findAllByBookId(UUID bookId);

    StockMovement save(StockMovement stockMovement);
}
