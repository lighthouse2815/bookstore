package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IBookService;
import com.bookstore.bookstore.presentation.mapper.BookWebMapper;
import com.bookstore.bookstore.presentation.request.CreateBookRequest;
import com.bookstore.bookstore.presentation.request.UpdateBookRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.BookResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final BookWebMapper bookWebMapper;

    @GetMapping("/api/books")
    public ApiResponse<List<BookResponse>> getAll() {
        return ApiResponse.success(bookService.getAll().stream()
                .map(bookWebMapper::toBookResponse)
                .toList());
    }

    @GetMapping("/api/books/search")
    public ApiResponse<List<BookResponse>> search(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(bookService.search(keyword).stream()
                .map(bookWebMapper::toBookResponse)
                .toList());
    }

    @GetMapping("/api/books/{id}")
    public ApiResponse<BookResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(bookWebMapper.toBookResponse(bookService.getById(id)));
    }

    @PostMapping("/api/admin/books")
    public ResponseEntity<ApiResponse<BookResponse>> create(@Valid @RequestBody CreateBookRequest request) {
        var result = bookService.create(bookWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bookWebMapper.toBookResponse(result)));
    }

    @PutMapping("/api/admin/books/{id}")
    public ApiResponse<BookResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookRequest request
    ) {
        var result = bookService.update(bookWebMapper.toUpdateCommand(id, request));
        return ApiResponse.success(bookWebMapper.toBookResponse(result));
    }

    @DeleteMapping("/api/admin/books/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        bookService.delete(bookWebMapper.toDeleteCommand(id));
        return ApiResponse.success("Deleted", null);
    }
}
