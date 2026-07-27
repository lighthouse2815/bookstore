package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.domain.enums.AuditAction;
import com.bookstore.bookstore.application.port.in.IOrderService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.presentation.mapper.OrderTimelineWebMapper;
import com.bookstore.bookstore.presentation.mapper.OrderWebMapper;
import com.bookstore.bookstore.presentation.request.CreateOrderRequest;
import com.bookstore.bookstore.presentation.request.CancelOrderRequest;
import com.bookstore.bookstore.presentation.request.UpdateOrderStatusRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.CreateOrderResponse;
import com.bookstore.bookstore.presentation.response.OrderResponse;
import com.bookstore.bookstore.presentation.response.OrderTimelineEventResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;
    private final IOrderTimelineService orderTimelineService;
    private final OrderWebMapper orderWebMapper;
    private final OrderTimelineWebMapper orderTimelineWebMapper;
    private final AdminAuditSupport adminAuditSupport;

    @PostMapping("/api/orders/checkout")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = orderService.checkout(orderWebMapper.toCreateOrderCommand(userId, request, idempotencyKey));
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
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
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

    @GetMapping("/api/orders/{id}/timeline")
    public ApiResponse<List<OrderTimelineEventResponse>> getMyOrderTimeline(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(orderTimelineService.getMyTimeline(userId, id).stream()
                .map(orderTimelineWebMapper::toResponse)
                .toList());
    }

    @PutMapping("/api/orders/{id}/cancel")
    public ApiResponse<OrderResponse> cancelMyOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody CancelOrderRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(orderWebMapper.toResponse(orderService.cancelMyOrder(
                new com.bookstore.bookstore.application.command.CancelOrderCommand(userId, id, request.reason())
        )));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = orderService.getAll(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
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

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/api/admin/orders/{id}/timeline")
    public ApiResponse<List<OrderTimelineEventResponse>> getOrderTimeline(@PathVariable UUID id) {
        return ApiResponse.success(orderTimelineService.getOrderTimeline(id).stream()
                .map(orderTimelineWebMapper::toResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/orders/{id}/status")
    public ApiResponse<OrderResponse> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse before = orderWebMapper.toResponse(orderService.getById(id));
        var result = orderService.updateStatus(orderWebMapper.toUpdateStatusCommand(id, request));
        OrderResponse response = orderWebMapper.toResponse(result);
        AuditAction action = request.status() == com.bookstore.bookstore.domain.enums.OrderStatus.CANCELLED
                ? AuditAction.ORDER_CANCELLED
                : AuditAction.ORDER_STATUS_UPDATED;
        adminAuditSupport.recordUpdate(
                jwt,
                httpServletRequest,
                action,
                AuditTargetType.ORDER,
                response.orderId(),
                "Cập nhật trạng thái đơn hàng " + response.orderCode() + " sang " + response.status(),
                before,
                response
        );
        return ApiResponse.success(response);
    }
}
