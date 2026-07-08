package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.ICartService;
import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.presentation.mapper.CartWebMapper;
import com.bookstore.bookstore.presentation.request.AddCartItemRequest;
import com.bookstore.bookstore.presentation.request.AddDigitalCartItemRequest;
import com.bookstore.bookstore.presentation.request.UpdateCartItemRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.BestCouponSuggestionResponse;
import com.bookstore.bookstore.presentation.response.CartResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bookstore.bookstore.domain.enums.ShippingMethod;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;
    private final ICouponService couponService;
    private final CartWebMapper cartWebMapper;

    @GetMapping
    public ApiResponse<CartResponse> getMyCart(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(cartWebMapper.toResponse(cartService.getMyCart(userId)));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = cartService.addItem(cartWebMapper.toAddCommand(userId, request));
        return ApiResponse.success(cartWebMapper.toResponse(result));
    }

    @PostMapping("/items/digital")
    public ApiResponse<CartResponse> addDigitalItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddDigitalCartItemRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = cartService.addItem(cartWebMapper.toAddDigitalCommand(userId, request));
        return ApiResponse.success(cartWebMapper.toResponse(result));
    }

    @GetMapping("/best-coupon")
    public ApiResponse<BestCouponSuggestionResponse> getBestCoupon(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) List<UUID> itemIds,
            @RequestParam(required = false) ShippingMethod shippingMethod
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = couponService.getBestCouponForCart(userId, itemIds, shippingMethod);
        return ApiResponse.success(new BestCouponSuggestionResponse(
                result.available(),
                result.couponCode(),
                result.couponType(),
                result.discountAmount(),
                result.finalAmountEstimate(),
                result.label(),
                result.reason()
        ));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartResponse> updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = cartService.updateItem(cartWebMapper.toUpdateCommand(userId, itemId, request));
        return ApiResponse.success(cartWebMapper.toResponse(result));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        cartService.removeItem(cartWebMapper.toRemoveCommand(userId, itemId));
        return ApiResponse.success("Deleted", null);
    }

    @DeleteMapping("/items")
    public ApiResponse<Void> clear(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        cartService.clear(userId);
        return ApiResponse.success("Cleared", null);
    }
}
