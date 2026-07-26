package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.ISupplierService;
import com.bookstore.bookstore.presentation.mapper.SupplierWebMapper;
import com.bookstore.bookstore.presentation.request.CreateSupplierRequest;
import com.bookstore.bookstore.presentation.request.UpdateSupplierRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.SupplierResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SupplierController {

    private final ISupplierService supplierService;
    private final SupplierWebMapper supplierWebMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = supplierService.getAll(page == null ? 0 : page, size == null ? 20 : size)
                    .map(supplierWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }
        return ResponseEntity.ok(ApiResponse.success(supplierService.getAll().stream()
                .map(supplierWebMapper::toResponse)
                .toList()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplierResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(supplierWebMapper.toResponse(supplierService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> create(@Valid @RequestBody CreateSupplierRequest request) {
        var result = supplierService.create(supplierWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(supplierWebMapper.toResponse(result)));
    }

    @PutMapping("/{id}")
    public ApiResponse<SupplierResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        var result = supplierService.update(supplierWebMapper.toUpdateCommand(id, request));
        return ApiResponse.success(supplierWebMapper.toResponse(result));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        supplierService.delete(supplierWebMapper.toDeleteCommand(id));
        return ApiResponse.success("Deleted", null);
    }
}
