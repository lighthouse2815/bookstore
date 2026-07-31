package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.ITransactionalOutboxService;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.domain.enums.OutboxStatus;
import com.bookstore.bookstore.presentation.mapper.OutboxWebMapper;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.OutboxEventResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/outbox")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OutboxController {
    private final ITransactionalOutboxService outboxService;
    private final OutboxWebMapper mapper;
    @GetMapping
    public ResponseEntity<ApiResponse<List<OutboxEventResponse>>> getPage(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) OutboxStatus status) {
        var result = outboxService.getPage(new PageQuery(page, size), status).map(mapper::toResponse);
        return ResponseEntity.ok().headers(PaginationHeaderUtils.build(result)).body(ApiResponse.success(result.items()));
    }
    @GetMapping("/{id}")
    public ApiResponse<OutboxEventResponse> getById(@PathVariable UUID id) { return ApiResponse.success(mapper.toResponse(outboxService.getById(id))); }
    @PostMapping("/{id}/retry")
    public ApiResponse<OutboxEventResponse> retry(@PathVariable UUID id) { return ApiResponse.success(mapper.toResponse(outboxService.retry(id))); }
}
