package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IOrderService;
import com.bookstore.bookstore.presentation.mapper.OrderWebMapper;
import com.bookstore.bookstore.presentation.request.CreateOrderRequest;
import com.bookstore.bookstore.presentation.request.UpdateOrderStatusRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.CreateOrderResponse;
import com.bookstore.bookstore.presentation.response.OrderResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;
    private final OrderWebMapper orderWebMapper;

    @PostMapping("/api/orders/checkout")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = orderService.checkout(orderWebMapper.toCreateOrderCommand(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderWebMapper.toCreateOrderResponse(result)));
    }

    @GetMapping("/api/orders/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        if (page != null || size != null) {
            var result = orderService.getMyOrders(
                    userId,
                    page == null ? 0 : page,
                    size == null ? 20 : size
            ).map(orderWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(userId).stream()
                .map(orderWebMapper::toResponse)
                .toList()));
    }

    @GetMapping("/api/orders/{id}")
    public ApiResponse<OrderResponse> getMyOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(orderWebMapper.toResponse(orderService.getMyOrder(userId, id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = orderService.getAll(
                    page == null ? 0 : page,
                    size == null ? 20 : size
            ).map(orderWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(orderService.getAll().stream()
                .map(orderWebMapper::toResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/orders/{id}")
    public ApiResponse<OrderResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(orderWebMapper.toResponse(orderService.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/orders/{id}/status")
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        var result = orderService.updateStatus(orderWebMapper.toUpdateStatusCommand(id, request));
        return ApiResponse.success(orderWebMapper.toResponse(result));
    }
}
