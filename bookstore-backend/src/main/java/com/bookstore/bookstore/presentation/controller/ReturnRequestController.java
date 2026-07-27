package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IReturnRequestService;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.presentation.mapper.ReturnRequestWebMapper;
import com.bookstore.bookstore.presentation.request.ApproveReturnRequestRequest;
import com.bookstore.bookstore.presentation.request.CreateReturnRequestRequest;
import com.bookstore.bookstore.presentation.request.RejectReturnRequestRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.response.ReturnRequestResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReturnRequestController {

    private final IReturnRequestService returnRequestService;
    private final ReturnRequestWebMapper returnRequestWebMapper;
    private final AdminAuditSupport adminAuditSupport;

    @PostMapping("/api/orders/{orderId}/return-request")
    public ResponseEntity<ApiResponse<ReturnRequestResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateReturnRequestRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = returnRequestService.create(
                returnRequestWebMapper.toCreateCommand(orderId, userId, request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(returnRequestWebMapper.toResponse(result)));
    }

    @GetMapping("/api/return-requests/my")
    public ResponseEntity<ApiResponse<List<ReturnRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) ReturnRequestStatus status,
            @RequestParam(required = false) UUID orderId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        if (page != null || size != null) {
            var result = returnRequestService.getMyRequests(
                    userId,
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    ),
                    status,
                    orderId
            ).map(returnRequestWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(returnRequestService.getMyRequests(userId, status, orderId).stream()
                .map(returnRequestWebMapper::toResponse)
                .toList()));
    }

    @GetMapping("/api/return-requests/{id}")
    public ApiResponse<ReturnRequestResponse> getMyRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(returnRequestWebMapper.toResponse(returnRequestService.getMyRequest(userId, id)));
    }

    @PutMapping("/api/return-requests/{id}/cancel")
    public ApiResponse<ReturnRequestResponse> cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(returnRequestWebMapper.toResponse(
                returnRequestService.cancel(returnRequestWebMapper.toCancelCommand(id, userId))
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/return-requests")
    public ResponseEntity<ApiResponse<List<ReturnRequestResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) ReturnRequestStatus status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID orderId
    ) {
        if (page != null || size != null) {
            var result = returnRequestService.getAll(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    ),
                    status,
                    userId,
                    orderId
            ).map(returnRequestWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(returnRequestService.getAll(status, userId, orderId).stream()
                .map(returnRequestWebMapper::toResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/return-requests/{id}")
    public ApiResponse<ReturnRequestResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(returnRequestWebMapper.toResponse(returnRequestService.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/return-requests/{id}/approve")
    public ApiResponse<ReturnRequestResponse> approve(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id,
            @Valid @RequestBody ApproveReturnRequestRequest request
    ) {
        ReturnRequestResponse before = returnRequestWebMapper.toResponse(returnRequestService.getById(id));
        UUID adminUserId = UUID.fromString(jwt.getSubject());
        ReturnRequestResponse response = returnRequestWebMapper.toResponse(returnRequestService.approve(
                returnRequestWebMapper.toApproveCommand(id, adminUserId, request)
        ));
        adminAuditSupport.recordUpdate(
                jwt,
                httpServletRequest,
                "RETURN_APPROVED",
                AuditTargetType.RETURN_REQUEST,
                response.id(),
                "Duyệt yêu cầu trả hàng cho đơn " + response.orderCode(),
                before,
                response
        );
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/return-requests/{id}/reject")
    public ApiResponse<ReturnRequestResponse> reject(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id,
            @Valid @RequestBody RejectReturnRequestRequest request
    ) {
        ReturnRequestResponse before = returnRequestWebMapper.toResponse(returnRequestService.getById(id));
        UUID adminUserId = UUID.fromString(jwt.getSubject());
        ReturnRequestResponse response = returnRequestWebMapper.toResponse(returnRequestService.reject(
                returnRequestWebMapper.toRejectCommand(id, adminUserId, request)
        ));
        adminAuditSupport.recordUpdate(
                jwt,
                httpServletRequest,
                "RETURN_REJECTED",
                AuditTargetType.RETURN_REQUEST,
                response.id(),
                "Từ chối yêu cầu trả hàng cho đơn " + response.orderCode(),
                before,
                response
        );
        return ApiResponse.success(response);
    }
}
