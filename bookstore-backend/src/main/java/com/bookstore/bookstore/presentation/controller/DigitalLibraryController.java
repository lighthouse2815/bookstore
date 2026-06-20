package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IDigitalAssetService;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.presentation.mapper.DigitalLibraryWebMapper;
import com.bookstore.bookstore.presentation.request.CreateDigitalAssetRequest;
import com.bookstore.bookstore.presentation.request.UpdateDigitalAssetRequest;
import com.bookstore.bookstore.presentation.request.UpdateReadingProgressRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.DigitalAssetResponse;
import com.bookstore.bookstore.presentation.response.DigitalLibraryAssetResponse;
import com.bookstore.bookstore.presentation.response.DigitalLibraryItemResponse;
import com.bookstore.bookstore.presentation.response.PublishedDigitalAssetResponse;
import com.bookstore.bookstore.presentation.response.ReadingProgressResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DigitalLibraryController {

    private final IDigitalAssetService digitalAssetService;
    private final IDigitalLibraryService digitalLibraryService;
    private final DigitalLibraryWebMapper digitalLibraryWebMapper;

    @GetMapping("/api/books/{bookId}/digital-assets")
    public ApiResponse<List<PublishedDigitalAssetResponse>> getPublishedDigitalAssets(@PathVariable UUID bookId) {
        return ApiResponse.success(digitalAssetService.getPublishedByBookId(bookId).stream()
                .map(digitalLibraryWebMapper::toPublishedDigitalAssetResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/books/{bookId}/digital-assets")
    public ApiResponse<List<DigitalAssetResponse>> getAdminDigitalAssets(@PathVariable UUID bookId) {
        return ApiResponse.success(digitalAssetService.getAllByBookIdForAdmin(bookId).stream()
                .map(digitalLibraryWebMapper::toDigitalAssetResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/books/{bookId}/digital-assets")
    public ResponseEntity<ApiResponse<DigitalAssetResponse>> createDigitalAsset(
            @PathVariable UUID bookId,
            @Valid @RequestBody CreateDigitalAssetRequest request
    ) {
        var result = digitalAssetService.create(digitalLibraryWebMapper.toCreateDigitalAssetCommand(bookId, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(digitalLibraryWebMapper.toDigitalAssetResponse(result)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/books/{bookId}/digital-assets/{digitalAssetId}")
    public ApiResponse<DigitalAssetResponse> updateDigitalAsset(
            @PathVariable UUID bookId,
            @PathVariable UUID digitalAssetId,
            @Valid @RequestBody UpdateDigitalAssetRequest request
    ) {
        var result = digitalAssetService.update(
                digitalLibraryWebMapper.toUpdateDigitalAssetCommand(bookId, digitalAssetId, request)
        );
        return ApiResponse.success(digitalLibraryWebMapper.toDigitalAssetResponse(result));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/books/{bookId}/digital-assets/{digitalAssetId}")
    public ApiResponse<Void> deleteDigitalAsset(
            @PathVariable UUID bookId,
            @PathVariable UUID digitalAssetId
    ) {
        digitalAssetService.delete(digitalLibraryWebMapper.toDeleteDigitalAssetCommand(bookId, digitalAssetId));
        return ApiResponse.success("Deleted", null);
    }

    @GetMapping("/api/digital-library/me/assets")
    public ApiResponse<List<DigitalLibraryItemResponse>> getMyLibrary(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(digitalLibraryService.getMyLibrary(userId).stream()
                .map(digitalLibraryWebMapper::toDigitalLibraryItemResponse)
                .toList());
    }

    @GetMapping("/api/digital-library/me/assets/{digitalAssetId}")
    public ApiResponse<DigitalLibraryAssetResponse> getMyLibraryAsset(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID digitalAssetId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(
                digitalLibraryWebMapper.toDigitalLibraryAssetResponse(
                        digitalLibraryService.getMyAsset(userId, digitalAssetId)
                )
        );
    }

    @PutMapping("/api/digital-library/me/assets/{digitalAssetId}/progress")
    public ApiResponse<ReadingProgressResponse> updateMyReadingProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID digitalAssetId,
            @Valid @RequestBody UpdateReadingProgressRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        var result = digitalLibraryService.updateMyProgress(
                digitalLibraryWebMapper.toUpdateReadingProgressCommand(userId, digitalAssetId, request)
        );
        return ApiResponse.success(digitalLibraryWebMapper.toReadingProgressResponse(result));
    }
}
