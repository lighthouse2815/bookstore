package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.presentation.mapper.CouponWebMapper;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.CouponResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicCouponController {

    private final ICouponService couponService;
    private final CouponWebMapper couponWebMapper;

    @GetMapping("/api/coupons/active")
    public ApiResponse<List<CouponResponse>> getActivePromotions() {
        return ApiResponse.success(couponService.getPublicActivePromotions(Instant.now()).stream()
                .map(couponWebMapper::toResponse)
                .toList());
    }
}
