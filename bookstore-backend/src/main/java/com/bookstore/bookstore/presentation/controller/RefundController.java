package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IRefundService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.presentation.mapper.RefundWebMapper;
import com.bookstore.bookstore.presentation.request.CancelRefundRequest;
import com.bookstore.bookstore.presentation.request.CreateRefundRequest;
import com.bookstore.bookstore.presentation.request.FailRefundRequest;
import com.bookstore.bookstore.presentation.request.SucceedRefundRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.response.RefundResponse;
import jakarta.validation.Valid;
import java.time.Instant;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RefundController {
    private final IRefundService refundService;
    private final RefundWebMapper mapper;

    @PostMapping("/orders/{orderId}/refunds")
    public ResponseEntity<ApiResponse<RefundResponse>> create(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId,
            @RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody CreateRefundRequest request) {
        RefundResponse response = mapper.toResponse(refundService.create(mapper.toCreate(orderId, userId(jwt), idempotencyKey, request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getPage(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) RefundMethod method, @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        var result = refundService.getPage(new PageQuery(page, size), status, method, from, to)
                .map(mapper::toResponse);
        return ResponseEntity.ok().headers(PaginationHeaderUtils.build(result)).body(ApiResponse.success(result.items()));
    }

    @GetMapping("/refunds/{id}")
    public ApiResponse<RefundResponse> getById(@PathVariable UUID id) { return ApiResponse.success(mapper.toResponse(refundService.getById(id))); }

    @PutMapping("/refunds/{id}/approve")
    public ApiResponse<RefundResponse> approve(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return ApiResponse.success(mapper.toResponse(refundService.approve(id, userId(jwt))));
    }
    @PutMapping("/refunds/{id}/processing")
    public ApiResponse<RefundResponse> processing(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return ApiResponse.success(mapper.toResponse(refundService.startProcessing(id, userId(jwt))));
    }
    @PutMapping("/refunds/{id}/succeed")
    public ApiResponse<RefundResponse> succeed(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody SucceedRefundRequest request) {
        return ApiResponse.success(mapper.toResponse(refundService.succeed(mapper.toSucceed(id, userId(jwt), request))));
    }
    @PutMapping("/refunds/{id}/fail")
    public ApiResponse<RefundResponse> fail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody FailRefundRequest request) {
        return ApiResponse.success(mapper.toResponse(refundService.fail(mapper.toFail(id, userId(jwt), request))));
    }
    @PutMapping("/refunds/{id}/cancel")
    public ApiResponse<RefundResponse> cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody(required = false) CancelRefundRequest request) {
        return ApiResponse.success(mapper.toResponse(refundService.cancel(mapper.toCancel(id, userId(jwt), request))));
    }
    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
