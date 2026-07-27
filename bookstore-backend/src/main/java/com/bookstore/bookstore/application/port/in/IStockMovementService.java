package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.StockMovementResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.UUID;

public interface IStockMovementService {

    List<StockMovementResult> getAll();

    PageSliceResult<StockMovementResult> getAll(PageQuery pageQuery);

    List<StockMovementResult> getByBookId(UUID bookId);
}
