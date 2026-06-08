package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.presentation.mapper.CouponWebMapper;
import com.bookstore.bookstore.presentation.request.CreateCouponRequest;
import com.bookstore.bookstore.presentation.request.UpdateCouponRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.CouponResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;
    private final CouponWebMapper couponWebMapper;

    @GetMapping
    public ApiResponse<List<CouponResponse>> getAll() {
        return ApiResponse.success(couponService.getAll().stream()
                .map(couponWebMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<CouponResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(couponWebMapper.toResponse(couponService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> create(@Valid @RequestBody CreateCouponRequest request) {
        var result = couponService.create(couponWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponWebMapper.toResponse(result)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CouponResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCouponRequest request
    ) {
        var result = couponService.update(couponWebMapper.toUpdateCommand(id, request));
        return ApiResponse.success(couponWebMapper.toResponse(result));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        couponService.delete(couponWebMapper.toDeleteCommand(id));
        return ApiResponse.success("Deleted", null);
    }
}
