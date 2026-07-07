package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IReviewService;
import com.bookstore.bookstore.presentation.mapper.ReviewWebMapper;
import com.bookstore.bookstore.presentation.request.CreateReviewRequest;
import com.bookstore.bookstore.presentation.request.UpdateReviewRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.response.ReviewResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;
    private final ReviewWebMapper reviewWebMapper;

    @GetMapping("/api/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByBookId(
            @PathVariable UUID bookId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = reviewService.getByBookId(
                    bookId,
                    page == null ? 0 : page,
                    size == null ? 10 : size
            ).map(reviewWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(reviewService.getByBookId(bookId).stream()
                .map(reviewWebMapper::toResponse)
                .toList()));
    }

    @PostMapping("/api/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID bookId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = reviewService.create(reviewWebMapper.toCreateCommand(userId, bookId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reviewWebMapper.toResponse(result)));
    }

    @PutMapping("/api/reviews/{id}")
    public ApiResponse<ReviewResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = reviewService.update(reviewWebMapper.toUpdateCommand(id, userId, request));
        return ApiResponse.success(reviewWebMapper.toResponse(result));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        reviewService.delete(reviewWebMapper.toDeleteCommand(id, userId));
        return ApiResponse.success("Deleted", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = reviewService.getAll(
                    page == null ? 0 : page,
                    size == null ? 10 : size
            ).map(reviewWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(reviewService.getAll().stream()
                .map(reviewWebMapper::toResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/reviews/{id}")
    public ApiResponse<Void> adminDelete(@PathVariable UUID id) {
        reviewService.adminDelete(id);
        return ApiResponse.success("Deleted", null);
    }
}
