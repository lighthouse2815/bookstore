package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IBookshelfService;
import com.bookstore.bookstore.presentation.mapper.BookshelfWebMapper;
import com.bookstore.bookstore.presentation.request.CreateBookshelfRequest;
import com.bookstore.bookstore.presentation.request.ReorderBookshelfItemsRequest;
import com.bookstore.bookstore.presentation.request.UpdateBookshelfRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.BookshelfResponse;
import com.bookstore.bookstore.presentation.response.BookshelfSummaryResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookshelves")
@RequiredArgsConstructor
public class BookshelfController {

    private final IBookshelfService bookshelfService;
    private final BookshelfWebMapper bookshelfWebMapper;

    @GetMapping
    public ApiResponse<List<BookshelfSummaryResponse>> getMyBookshelves(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(bookshelfService.getMyBookshelves(userId).stream()
                .map(bookshelfWebMapper::toSummaryResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookshelfResponse>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBookshelfRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        BookshelfResponse response = bookshelfWebMapper.toResponse(
                bookshelfService.create(bookshelfWebMapper.toCreateCommand(userId, request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{shelfId}")
    public ApiResponse<BookshelfResponse> getMyBookshelf(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shelfId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(bookshelfWebMapper.toResponse(
                bookshelfService.getMyBookshelf(userId, shelfId)
        ));
    }

    @PutMapping("/{shelfId}")
    public ApiResponse<BookshelfResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shelfId,
            @Valid @RequestBody UpdateBookshelfRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(bookshelfWebMapper.toResponse(
                bookshelfService.update(bookshelfWebMapper.toUpdateCommand(shelfId, userId, request))
        ));
    }

    @DeleteMapping("/{shelfId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shelfId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        bookshelfService.delete(bookshelfWebMapper.toDeleteCommand(shelfId, userId));
        return ApiResponse.success("Deleted", null);
    }

    @PostMapping("/{shelfId}/items/{bookId}")
    public ApiResponse<BookshelfResponse> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shelfId,
            @PathVariable UUID bookId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(bookshelfWebMapper.toResponse(
                bookshelfService.addItem(bookshelfWebMapper.toAddItemCommand(shelfId, userId, bookId))
        ));
    }

    @DeleteMapping("/{shelfId}/items/{bookId}")
    public ApiResponse<BookshelfResponse> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shelfId,
            @PathVariable UUID bookId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(bookshelfWebMapper.toResponse(
                bookshelfService.removeItem(bookshelfWebMapper.toRemoveItemCommand(shelfId, userId, bookId))
        ));
    }

    @PutMapping("/{shelfId}/items/reorder")
    public ApiResponse<BookshelfResponse> reorderItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID shelfId,
            @Valid @RequestBody ReorderBookshelfItemsRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(bookshelfWebMapper.toResponse(
                bookshelfService.reorderItems(bookshelfWebMapper.toReorderCommand(shelfId, userId, request))
        ));
    }
}
