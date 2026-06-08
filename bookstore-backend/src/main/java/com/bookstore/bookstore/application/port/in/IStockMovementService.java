package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.result.StockMovementResult;
import java.util.List;
import java.util.UUID;

public interface IStockMovementService {

    List<StockMovementResult> getAll();

    List<StockMovementResult> getByBookId(UUID bookId);
}
