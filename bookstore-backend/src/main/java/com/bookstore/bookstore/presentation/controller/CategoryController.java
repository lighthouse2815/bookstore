package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.ICategoryService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.presentation.mapper.CategoryWebMapper;
import com.bookstore.bookstore.presentation.request.CreateCategoryRequest;
import com.bookstore.bookstore.presentation.request.UpdateCategoryRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.CategoryResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;
    private final CategoryWebMapper categoryWebMapper;

    @GetMapping("/api/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = categoryService.getAll(new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    ))
                    .map(categoryWebMapper::toCategoryResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(categoryService.getAll().stream()
                .map(categoryWebMapper::toCategoryResponse)
                .toList()));
    }

    @GetMapping("/api/categories/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(categoryWebMapper.toCategoryResponse(categoryService.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CreateCategoryRequest request) {
        var result = categoryService.create(categoryWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(categoryWebMapper.toCategoryResponse(result)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/categories/{id}")
    public ApiResponse<CategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        var result = categoryService.update(categoryWebMapper.toUpdateCommand(id, request));
        return ApiResponse.success(categoryWebMapper.toCategoryResponse(result));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/categories/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        categoryService.delete(categoryWebMapper.toDeleteCommand(id));
        return ApiResponse.success("Deleted", null);
    }
}
