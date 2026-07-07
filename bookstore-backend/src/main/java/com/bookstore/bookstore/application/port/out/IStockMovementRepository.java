package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.UUID;

public interface IStockMovementRepository {

    List<StockMovement> findAll();

    PageSliceResult<StockMovement> findPage(int page, int size);

    List<StockMovement> findAllByBookId(UUID bookId);

    StockMovement save(StockMovement stockMovement);
}
