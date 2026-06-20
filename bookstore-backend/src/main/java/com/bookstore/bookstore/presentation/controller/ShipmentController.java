package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IShipmentService;
import com.bookstore.bookstore.presentation.mapper.ShipmentWebMapper;
import com.bookstore.bookstore.presentation.request.AssignShipmentRequest;
import com.bookstore.bookstore.presentation.request.UpdateShipmentStatusRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.ShipmentResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShipmentController {

    private final IShipmentService shipmentService;
    private final ShipmentWebMapper shipmentWebMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/shipments")
    public ResponseEntity<ApiResponse<ShipmentResponse>> assign(@Valid @RequestBody AssignShipmentRequest request) {
        ShipmentResponse response = shipmentWebMapper.toResponse(
                shipmentService.assign(shipmentWebMapper.toAssignCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/shipments")
    public ApiResponse<List<ShipmentResponse>> getAll() {
        return ApiResponse.success(shipmentService.getAll().stream()
                .map(shipmentWebMapper::toResponse)
                .toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/shipments/{id}")
    public ApiResponse<ShipmentResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(shipmentWebMapper.toResponse(shipmentService.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/shipments/{id}/confirm-delivered")
    public ApiResponse<ShipmentResponse> confirmDelivered(@PathVariable UUID id) {
        return ApiResponse.success(shipmentWebMapper.toResponse(shipmentService.confirmDeliveredByAdmin(id)));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping("/api/shipper/shipments/my")
    public ApiResponse<List<ShipmentResponse>> getMyShipments(@AuthenticationPrincipal Jwt jwt) {
        UUID shipperId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(shipmentService.getMyShipments(shipperId).stream()
                .map(shipmentWebMapper::toResponse)
                .toList());
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping("/api/shipper/shipments/{id}")
    public ApiResponse<ShipmentResponse> getMyShipment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID shipperId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(shipmentWebMapper.toResponse(shipmentService.getMyShipment(shipperId, id)));
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @PutMapping("/api/shipper/shipments/{id}/status")
    public ApiResponse<ShipmentResponse> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        UUID shipperId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success(shipmentWebMapper.toResponse(
                shipmentService.updateMyShipmentStatus(
                        shipmentWebMapper.toUpdateStatusCommand(id, shipperId, request)
                )
        ));
    }
}
