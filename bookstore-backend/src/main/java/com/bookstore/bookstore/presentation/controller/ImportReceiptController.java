package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IImportReceiptService;
import com.bookstore.bookstore.presentation.mapper.ImportReceiptWebMapper;
import com.bookstore.bookstore.presentation.request.CreateImportReceiptRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.ImportReceiptResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/import-receipts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ImportReceiptController {

    private final IImportReceiptService importReceiptService;
    private final ImportReceiptWebMapper importReceiptWebMapper;

    @GetMapping
    public ApiResponse<List<ImportReceiptResponse>> getAll() {
        return ApiResponse.success(importReceiptService.getAll().stream()
                .map(importReceiptWebMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<ImportReceiptResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(importReceiptWebMapper.toResponse(importReceiptService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImportReceiptResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateImportReceiptRequest request
    ) {
        UUID createdBy = UUID.fromString(jwt.getSubject());
        var result = importReceiptService.create(importReceiptWebMapper.toCreateCommand(createdBy, request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(importReceiptWebMapper.toResponse(result)));
    }
}
