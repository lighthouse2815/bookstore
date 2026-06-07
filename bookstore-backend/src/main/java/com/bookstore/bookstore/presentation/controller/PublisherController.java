package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IPublisherService;
import com.bookstore.bookstore.presentation.mapper.PublisherWebMapper;
import com.bookstore.bookstore.presentation.request.CreatePublisherRequest;
import com.bookstore.bookstore.presentation.request.UpdatePublisherRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PublisherResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublisherController {

    private final IPublisherService publisherService;
    private final PublisherWebMapper publisherWebMapper;

    @GetMapping("/api/publishers")
    public ApiResponse<List<PublisherResponse>> getAll() {
        return ApiResponse.success(publisherService.getAll().stream()
                .map(publisherWebMapper::toPublisherResponse)
                .toList());
    }

    @GetMapping("/api/publishers/{id}")
    public ApiResponse<PublisherResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(publisherWebMapper.toPublisherResponse(publisherService.getById(id)));
    }

    @PostMapping("/api/admin/publishers")
    public ResponseEntity<ApiResponse<PublisherResponse>> create(@Valid @RequestBody CreatePublisherRequest request) {
        var result = publisherService.create(publisherWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(publisherWebMapper.toPublisherResponse(result)));
    }

    @PutMapping("/api/admin/publishers/{id}")
    public ApiResponse<PublisherResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePublisherRequest request
    ) {
        var result = publisherService.update(publisherWebMapper.toUpdateCommand(id, request));
        return ApiResponse.success(publisherWebMapper.toPublisherResponse(result));
    }

    @DeleteMapping("/api/admin/publishers/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        publisherService.delete(publisherWebMapper.toDeleteCommand(id));
        return ApiResponse.success("Deleted", null);
    }
}
