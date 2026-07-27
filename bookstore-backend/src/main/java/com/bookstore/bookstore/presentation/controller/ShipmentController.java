package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IShipmentService;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.presentation.mapper.ShipmentWebMapper;
import com.bookstore.bookstore.presentation.request.AssignShipmentRequest;
import com.bookstore.bookstore.presentation.request.UpdateShipmentStatusRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.bookstore.bookstore.presentation.response.PaginationHeaderUtils;
import com.bookstore.bookstore.presentation.response.ShipmentResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShipmentController {

    private final IShipmentService shipmentService;
    private final ShipmentWebMapper shipmentWebMapper;
    private final AdminAuditSupport adminAuditSupport;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/shipments")
    public ResponseEntity<ApiResponse<ShipmentResponse>> assign(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody AssignShipmentRequest request
    ) {
        ShipmentResponse response = shipmentWebMapper.toResponse(
                shipmentService.assign(shipmentWebMapper.toAssignCommand(request))
        );
        adminAuditSupport.recordCreate(
                jwt,
                httpServletRequest,
                "SHIPMENT_ASSIGNED",
                AuditTargetType.SHIPMENT,
                response.shipmentId(),
                "Phân công shipment cho đơn hàng " + response.orderCode(),
                response
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/shipments")
    public ResponseEntity<ApiResponse<List<ShipmentResponse>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null || size != null) {
            var result = shipmentService.getAll(
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            ).map(shipmentWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(shipmentService.getAll().stream()
                .map(shipmentWebMapper::toResponse)
                .toList()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/shipments/{id}")
    public ApiResponse<ShipmentResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(shipmentWebMapper.toResponse(shipmentService.getById(id)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/admin/shipments/{id}/confirm-delivered")
    public ApiResponse<ShipmentResponse> confirmDelivered(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest,
            @PathVariable UUID id
    ) {
        ShipmentResponse before = shipmentWebMapper.toResponse(shipmentService.getById(id));
        ShipmentResponse response = shipmentWebMapper.toResponse(shipmentService.confirmDeliveredByAdmin(id));
        adminAuditSupport.recordStatusChange(
                jwt,
                httpServletRequest,
                "SHIPMENT_STATUS_UPDATED",
                AuditTargetType.SHIPMENT,
                response.shipmentId(),
                "Xác nhận shipment " + response.shipmentId() + " đã giao thành công",
                before,
                response
        );
        return ApiResponse.success(response);
    }

    @PreAuthorize("hasRole('SHIPPER')")
    @GetMapping("/api/shipper/shipments/my")
    public ResponseEntity<ApiResponse<List<ShipmentResponse>>> getMyShipments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        UUID shipperId = UUID.fromString(jwt.getSubject());
        if (page != null || size != null) {
            var result = shipmentService.getMyShipments(
                    shipperId,
                    new PageQuery(
                            page == null ? PageQuery.DEFAULT_PAGE : page,
                            size == null ? PageQuery.DEFAULT_SIZE : size
                    )
            ).map(shipmentWebMapper::toResponse);
            return ResponseEntity.ok()
                    .headers(PaginationHeaderUtils.build(result))
                    .body(ApiResponse.success(result.items()));
        }

        return ResponseEntity.ok(ApiResponse.success(shipmentService.getMyShipments(shipperId).stream()
                .map(shipmentWebMapper::toResponse)
                .toList()));
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
