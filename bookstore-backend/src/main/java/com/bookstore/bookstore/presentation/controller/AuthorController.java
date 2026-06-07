package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IAuthorService;
import com.bookstore.bookstore.presentation.mapper.AuthorWebMapper;
import com.bookstore.bookstore.presentation.request.CreateAuthorRequest;
import com.bookstore.bookstore.presentation.request.UpdateAuthorRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.AuthorResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthorController {

    private final IAuthorService authorService;
    private final AuthorWebMapper authorWebMapper;

    @GetMapping("/api/authors")
    public ApiResponse<List<AuthorResponse>> getAll() {
        return ApiResponse.success(authorService.getAll().stream()
                .map(authorWebMapper::toAuthorResponse)
                .toList());
    }

    @GetMapping("/api/authors/{id}")
    public ApiResponse<AuthorResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(authorWebMapper.toAuthorResponse(authorService.getById(id)));
    }

    @PostMapping("/api/admin/authors")
    public ResponseEntity<ApiResponse<AuthorResponse>> create(@Valid @RequestBody CreateAuthorRequest request) {
        var result = authorService.create(authorWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authorWebMapper.toAuthorResponse(result)));
    }

    @PutMapping("/api/admin/authors/{id}")
    public ApiResponse<AuthorResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAuthorRequest request
    ) {
        var result = authorService.update(authorWebMapper.toUpdateCommand(id, request));
        return ApiResponse.success(authorWebMapper.toAuthorResponse(result));
    }

    @DeleteMapping("/api/admin/authors/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        authorService.delete(authorWebMapper.toDeleteCommand(id));
        return ApiResponse.success("Deleted", null);
    }
}
