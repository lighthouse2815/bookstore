package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.domain.enums.AuditAction;
import com.bookstore.bookstore.application.port.in.IPaymentReconciliationService;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PaymentReconciliationIssueResult;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationIssueType;
import com.bookstore.bookstore.domain.enums.PaymentReconciliationStatus;
import com.bookstore.bookstore.presentation.request.ResolvePaymentReconciliationRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PaymentReconciliationIssueResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.support.AdminAuditSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PaymentReconciliationController {
    private final IPaymentReconciliationService reconciliationService;
    private final AdminAuditSupport adminAuditSupport;

    @GetMapping("/api/admin/payment-reconciliation")
    public ResponseEntity<ApiResponse<List<PaymentReconciliationIssueResponse>>> getPage(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PaymentReconciliationStatus status,
            @RequestParam(required = false) PaymentReconciliationIssueType issueType,
            @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to
    ) {
        var result = reconciliationService.getPage(new PageQuery(page, size), status, issueType, from, to)
                .map(this::toResponse);
        return ResponseEntity.ok().headers(PaginationHeaderUtils.build(result)).body(ApiResponse.success(result.items()));
    }

    @GetMapping("/api/admin/payment-reconciliation/{id}")
    public ApiResponse<PaymentReconciliationIssueResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(toResponse(reconciliationService.getById(id)));
    }

    @PutMapping("/api/admin/payment-reconciliation/{id}/resolve")
    public ApiResponse<PaymentReconciliationIssueResponse> resolve(
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest request, @PathVariable UUID id,
            @Valid @RequestBody ResolvePaymentReconciliationRequest body
    ) {
        PaymentReconciliationIssueResponse before = toResponse(reconciliationService.getById(id));
        PaymentReconciliationIssueResponse after = toResponse(reconciliationService.resolve(
                id, UUID.fromString(jwt.getSubject()), body.resolutionNote()
        ));
        adminAuditSupport.recordUpdate(
                jwt, request, AuditAction.PAYMENT_RECONCILIATION_RESOLVED, AuditTargetType.PAYMENT_RECONCILIATION, id,
                "Đã xử lý vấn đề đối soát thanh toán " + id, before, after
        );
        return ApiResponse.success(after);
    }

    private PaymentReconciliationIssueResponse toResponse(PaymentReconciliationIssueResult item) {
        return new PaymentReconciliationIssueResponse(item.id(), item.paymentId(), item.orderId(), item.issueType(),
                item.expectedAmount(), item.receivedAmount(), item.externalTransactionId(), item.details(), item.status(),
                item.detectedAt(), item.resolvedAt(), item.resolvedBy(), item.resolutionNote(), item.createdAt(), item.updatedAt());
    }
}
