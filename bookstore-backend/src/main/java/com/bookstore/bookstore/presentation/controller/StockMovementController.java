package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IStockMovementService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.presentation.mapper.StockMovementWebMapper;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.StockMovementResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
public class StockMovementController {

    private final IStockMovementService stockMovementService;
    private final StockMovementWebMapper stockMovementWebMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/stock-movements")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = stockMovementService.getAll(new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    ))
                    .map(stockMovementWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }
        return ResponseEntity.ok(ApiResponse.success(stockMovementService.getAll().stream()
                .map(stockMovementWebMapper::toResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/books/{bookId}/stock-movements")
    public ApiResponse<List<StockMovementResponse>> getByBookId(@PathVariable UUID bookId) {
        return ApiResponse.success(stockMovementService.getByBookId(bookId).stream()
                .map(stockMovementWebMapper::toResponse)
                .toList());
    }
}
