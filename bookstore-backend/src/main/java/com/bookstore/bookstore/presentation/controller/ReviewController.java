package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.application.port.in.IReviewService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.presentation.mapper.ReviewWebMapper;
import com.bookstore.bookstore.presentation.request.CreateReviewRequest;
import com.bookstore.bookstore.presentation.request.ReviewModerationRequest;
import com.bookstore.bookstore.presentation.request.UpdateReviewRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.response.PublicReviewResponse;
import com.bookstore.bookstore.presentation.response.ReviewResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;
    private final ReviewWebMapper reviewWebMapper;
    private final AdminAuditSupport adminAuditSupport;

    @GetMapping("/api/books/{bookId}/reviews")
    public ResponseEntity<ApiResponse<List<PublicReviewResponse>>> getByBookId(
            @PathVariable UUID bookId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = reviewService.getByBookId(
                    bookId,
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? 10 : size
                    )
            ).map(reviewWebMapper::toPublicResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(reviewService.getByBookId(bookId).stream()
                .map(reviewWebMapper::toPublicResponse)
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
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Integer rating
    ) {
        if (page != null || size != null || status != null || bookId != null || userId != null || rating != null) {
            var result = reviewService.getAll(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? 10 : size
                    ),
                    status,
                    bookId,
                    userId,
                    rating
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
    @PutMapping("/api/admin/reviews/{id}/hide")
    public ApiResponse<ReviewResponse> hide(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ReviewModerationRequest request
    ) {
        UUID adminUserId = UUID.fromString(jwt.getSubject());
        ReviewResponse response = reviewWebMapper.toResponse(
                reviewService.hide(reviewWebMapper.toHideCommand(id, adminUserId, request))
        );
        adminAuditSupport.recordStatusChange(
                jwt,
                httpServletRequest,
                "REVIEW_HIDDEN",
                "REVIEW",
                id,
                "An danh gia " + id,
                null,
                response
        );
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/reviews/{id}/approve")
    public ApiResponse<ReviewResponse> approve(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id
    ) {
        UUID adminUserId = UUID.fromString(jwt.getSubject());
        ReviewResponse response = reviewWebMapper.toResponse(
                reviewService.approve(reviewWebMapper.toApproveCommand(id, adminUserId))
        );
        adminAuditSupport.recordStatusChange(
                jwt,
                httpServletRequest,
                "REVIEW_APPROVED",
                "REVIEW",
                id,
                "Phe duyet danh gia " + id,
                null,
                response
        );
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/reviews/{id}")
    public ApiResponse<Void> adminDelete(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id
    ) {
        ReviewResponse deletedReview = reviewWebMapper.toResponse(reviewService.adminDelete(id));
        adminAuditSupport.recordDelete(
                jwt,
                httpServletRequest,
                "REVIEW_DELETED",
                "REVIEW",
                id,
                "Xoa mem danh gia " + id,
                deletedReview
        );
        return ApiResponse.success("Deleted", null);
    }
}
