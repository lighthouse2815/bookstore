package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IFileAssetService;
import com.bookstore.bookstore.presentation.mapper.FileWebMapper;
import com.bookstore.bookstore.presentation.request.CompleteFileUploadRequest;
import com.bookstore.bookstore.presentation.request.PresignUploadRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.FileAssetResponse;
import com.bookstore.bookstore.presentation.response.PresignedUploadResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final IFileAssetService fileAssetService;
    private final FileWebMapper fileWebMapper;

    @PostMapping("/api/files/presign-upload")
    public ResponseEntity<ApiResponse<PresignedUploadResponse>> createPresignedUpload(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PresignUploadRequest request
    ) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean admin = hasRole(jwt, "ADMIN");
        var result = fileAssetService.createPresignedUpload(
                fileWebMapper.toPresignUploadCommand(requesterId, admin, request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fileWebMapper.toPresignedUploadResponse(result)));
    }

    @PostMapping("/api/files/complete-upload")
    public ApiResponse<FileAssetResponse> completeUpload(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CompleteFileUploadRequest request
    ) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean admin = hasRole(jwt, "ADMIN");
        var result = fileAssetService.completeUpload(
                fileWebMapper.toCompleteUploadCommand(requesterId, admin, request)
        );
        return ApiResponse.success(fileWebMapper.toFileAssetResponse(result));
    }

    @GetMapping("/api/files/{id}")
    public ApiResponse<FileAssetResponse> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean admin = hasRole(jwt, "ADMIN");
        return ApiResponse.success(
                fileWebMapper.toFileAssetResponse(fileAssetService.getById(id, requesterId, admin))
        );
    }

    @DeleteMapping("/api/files/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        boolean admin = hasRole(jwt, "ADMIN");
        fileAssetService.delete(id, requesterId, admin);
        return ApiResponse.success("Deleted", null);
    }

    private boolean hasRole(Jwt jwt, String roleName) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.stream().anyMatch(roleName::equalsIgnoreCase);
    }
}
