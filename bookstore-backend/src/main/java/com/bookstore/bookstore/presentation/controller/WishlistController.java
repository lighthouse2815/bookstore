package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IWishlistService;
import com.bookstore.bookstore.presentation.mapper.BookWebMapper;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.BookResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final IWishlistService wishlistService;
    private final BookWebMapper bookWebMapper;

    @GetMapping
    public ApiResponse<List<BookResponse>> getMyWishlist(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(wishlistService.getMyWishlist(userId).stream()
                .map(bookWebMapper::toBookResponse)
                .toList());
    }

    @PostMapping("/items/{bookId}")
    public ApiResponse<Void> addBook(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID bookId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        wishlistService.addBook(userId, bookId);
        return ApiResponse.success("Added", null);
    }

    @DeleteMapping("/items/{bookId}")
    public ApiResponse<Void> removeBook(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID bookId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        wishlistService.removeBook(userId, bookId);
        return ApiResponse.success("Deleted", null);
    }
}
