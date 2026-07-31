package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.domain.enums.AuditAction;
import com.bookstore.bookstore.application.port.in.IBookService;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.application.port.in.IBookQueryService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.presentation.mapper.BookWebMapper;
import com.bookstore.bookstore.presentation.request.CreateBookRequest;
import com.bookstore.bookstore.presentation.request.UpdateBookRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.BookPageDetailResponse;
import com.bookstore.bookstore.presentation.response.BookResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
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
public class BookController {

    private final IBookService bookService;
    private final IBookQueryService bookQueryService;
    private final BookWebMapper bookWebMapper;
    private final AdminAuditSupport adminAuditSupport;

    @GetMapping("/api/books")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = bookQueryService.getAll(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            ).map(bookWebMapper::toBookResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(bookQueryService.getAll().stream()
                .map(bookWebMapper::toBookResponse)
                .toList()));
    }

    @GetMapping("/api/books/search")
    public ResponseEntity<ApiResponse<List<BookResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null || categoryId != null) {
            var result = bookQueryService.search(
                    keyword,
                    categoryId,
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            ).map(bookWebMapper::toBookResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(bookQueryService.search(keyword).stream()
                .map(bookWebMapper::toBookResponse)
                .toList()));
    }

    @GetMapping("/api/books/{id}/related")
    public ApiResponse<List<BookResponse>> getRelatedBooks(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return ApiResponse.success(bookQueryService.getRelatedBooks(id, limit).stream()
                .map(bookWebMapper::toBookResponse)
                .toList());
    }

    @GetMapping("/api/books/{id}/page-detail")
    public ApiResponse<BookPageDetailResponse> getPageDetail(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "8") int relatedLimit
    ) {
        return ApiResponse.success(bookWebMapper.toBookPageDetailResponse(bookQueryService.getPageDetail(id, relatedLimit)));
    }

    @GetMapping("/api/books/{id}")
    public ApiResponse<BookResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(bookWebMapper.toBookResponse(bookQueryService.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/books")
    public ResponseEntity<ApiResponse<BookResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CreateBookRequest request
    ) {
        var result = bookService.create(bookWebMapper.toCreateCommand(request));
        BookResponse response = bookWebMapper.toBookResponse(result);
        adminAuditSupport.recordCreate(
                jwt,
                httpServletRequest,
                AuditAction.BOOK_CREATED,
                AuditTargetType.BOOK,
                response.id(),
                "Tạo sách " + response.title(),
                response
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/books/{id}")
    public ApiResponse<BookResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookRequest request
    ) {
        BookResponse before = bookWebMapper.toBookResponse(bookQueryService.getById(id));
        var result = bookService.update(bookWebMapper.toUpdateCommand(id, request));
        BookResponse response = bookWebMapper.toBookResponse(result);
        adminAuditSupport.recordUpdate(
                jwt,
                httpServletRequest,
                AuditAction.BOOK_UPDATED,
                AuditTargetType.BOOK,
                response.id(),
                "Cập nhật sách " + response.title(),
                before,
                response
        );
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/admin/books/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id
    ) {
        BookResponse before = bookWebMapper.toBookResponse(bookQueryService.getById(id));
        bookService.delete(bookWebMapper.toDeleteCommand(id));
        adminAuditSupport.recordDelete(
                jwt,
                httpServletRequest,
                AuditAction.BOOK_DELETED,
                AuditTargetType.BOOK,
                id,
                "Xóa sách " + before.title(),
                before
        );
        return ApiResponse.success("Deleted", null);
    }
}
