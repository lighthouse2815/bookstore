package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IStockMovementService;
import com.bookstore.bookstore.presentation.mapper.StockMovementWebMapper;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.StockMovementResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StockMovementController {

    private final IStockMovementService stockMovementService;
    private final StockMovementWebMapper stockMovementWebMapper;

    @GetMapping("/api/admin/stock-movements")
    public ApiResponse<List<StockMovementResponse>> getAll() {
        return ApiResponse.success(stockMovementService.getAll().stream()
                .map(stockMovementWebMapper::toResponse)
                .toList());
    }

    @GetMapping("/api/admin/books/{bookId}/stock-movements")
    public ApiResponse<List<StockMovementResponse>> getByBookId(@PathVariable UUID bookId) {
        return ApiResponse.success(stockMovementService.getByBookId(bookId).stream()
                .map(stockMovementWebMapper::toResponse)
                .toList());
    }
}
