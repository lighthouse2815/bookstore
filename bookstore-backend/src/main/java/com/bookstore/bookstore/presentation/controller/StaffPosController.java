package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IOrderService;
import com.bookstore.bookstore.presentation.mapper.OrderWebMapper;
import com.bookstore.bookstore.presentation.mapper.PosOrderWebMapper;
import com.bookstore.bookstore.presentation.request.CreatePosOrderRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.CreatePosOrderResponse;
import com.bookstore.bookstore.presentation.response.OrderResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/pos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class StaffPosController {

    private final IOrderService orderService;
    private final PosOrderWebMapper posOrderWebMapper;
    private final OrderWebMapper orderWebMapper;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<CreatePosOrderResponse>> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePosOrderRequest request
    ) {
        UUID staffUserId = UUID.fromString(jwt.getSubject());
        var result = orderService.createPosOrder(posOrderWebMapper.toCommand(staffUserId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(posOrderWebMapper.toResponse(result)));
    }

    @GetMapping("/orders")
    public ApiResponse<List<OrderResponse>> getOrders() {
        return ApiResponse.success(orderService.getAll().stream()
                .map(orderWebMapper::toResponse)
                .toList());
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable UUID id) {
        return ApiResponse.success(orderWebMapper.toResponse(orderService.getById(id)));
    }
}
