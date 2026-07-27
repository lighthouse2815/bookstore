package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.presentation.mapper.CouponWebMapper;
import com.bookstore.bookstore.presentation.request.CreateCouponRequest;
import com.bookstore.bookstore.presentation.request.UpdateCouponRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.CouponResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CouponController {

    private final ICouponService couponService;
    private final CouponWebMapper couponWebMapper;
    private final AdminAuditSupport adminAuditSupport;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = couponService.getAll(new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    ))
                    .map(couponWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }
        return ResponseEntity.ok(ApiResponse.success(couponService.getAll().stream()
                .map(couponWebMapper::toResponse)
                .toList()));
    }

    @GetMapping("/{id}")
    public ApiResponse<CouponResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(couponWebMapper.toResponse(couponService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CreateCouponRequest request
    ) {
        var result = couponService.create(couponWebMapper.toCreateCommand(request));
        CouponResponse response = couponWebMapper.toResponse(result);
        adminAuditSupport.recordCreate(
                jwt,
                httpServletRequest,
                "COUPON_CREATED",
                AuditTargetType.COUPON,
                response.id(),
                "Tạo coupon " + response.code(),
                response
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ApiResponse<CouponResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCouponRequest request
    ) {
        CouponResponse before = couponWebMapper.toResponse(couponService.getById(id));
        var result = couponService.update(couponWebMapper.toUpdateCommand(id, request));
        CouponResponse response = couponWebMapper.toResponse(result);
        adminAuditSupport.recordUpdate(
                jwt,
                httpServletRequest,
                "COUPON_UPDATED",
                AuditTargetType.COUPON,
                response.id(),
                "Cập nhật coupon " + response.code(),
                before,
                response
        );
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id
    ) {
        CouponResponse before = couponWebMapper.toResponse(couponService.getById(id));
        couponService.delete(couponWebMapper.toDeleteCommand(id));
        adminAuditSupport.recordDelete(
                jwt,
                httpServletRequest,
                "COUPON_DELETED",
                AuditTargetType.COUPON,
                id,
                "Xóa coupon " + before.code(),
                before
        );
        return ApiResponse.success("Deleted", null);
    }
}
