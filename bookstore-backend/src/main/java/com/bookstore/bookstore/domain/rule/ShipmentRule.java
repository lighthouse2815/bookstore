package com.bookstore.bookstore.domain.rule;

import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;

public final class ShipmentRule {

    private ShipmentRule() {
    }

    public static void requireStatusChanged(ShipmentStatus currentStatus, ShipmentStatus nextStatus) {
        if (currentStatus == nextStatus) {
            throw new DomainException(DomainErrorCode.SHIPMENT_STATUS_NOT_CHANGED);
        }
    }

    public static void requireCanMarkPickedUp(ShipmentStatus currentStatus) {
        if (currentStatus != ShipmentStatus.ASSIGNED) {
            throw new DomainException(
                    DomainErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION,
                    currentStatus,
                    ShipmentStatus.PICKED_UP
            );
        }
    }

    public static void requireCanStartDelivering(ShipmentStatus currentStatus) {
        if (currentStatus != ShipmentStatus.PICKED_UP) {
            throw new DomainException(
                    DomainErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION,
                    currentStatus,
                    ShipmentStatus.DELIVERING
            );
        }
    }

    public static void requireCanMarkDelivered(ShipmentStatus currentStatus) {
        if (currentStatus != ShipmentStatus.DELIVERING) {
            throw new DomainException(
                    DomainErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION,
                    currentStatus,
                    ShipmentStatus.DELIVERED
            );
        }
    }

    public static void requireCanMarkFailed(ShipmentStatus currentStatus) {
        if (currentStatus == ShipmentStatus.DELIVERED || currentStatus == ShipmentStatus.FAILED) {
            throw new DomainException(
                    DomainErrorCode.INVALID_SHIPMENT_STATUS_TRANSITION,
                    currentStatus,
                    ShipmentStatus.FAILED
            );
        }
    }

    public static void requireFailureReasonConsistent(ShipmentStatus status, String failureReason) {
        boolean hasReason = failureReason != null;

        if (status == ShipmentStatus.FAILED && !hasReason) {
            throw new DomainException(DomainErrorCode.INVALID_SHIPMENT_FAILURE_REASON, "failureReason");
        }

        if (status != ShipmentStatus.FAILED && hasReason) {
            throw new DomainException(DomainErrorCode.INVALID_SHIPMENT_FAILURE_REASON, "failureReason");
        }
    }

    public static void requireStatusTimelineConsistent(
            ShipmentStatus status,
            Instant pickedUpAt,
            Instant deliveringAt,
            Instant deliveredAt,
            Instant failedAt
    ) {
        switch (status) {
            case ASSIGNED -> {
                if (pickedUpAt != null || deliveringAt != null || deliveredAt != null || failedAt != null) {
                    throw new DomainException(DomainErrorCode.INVALID_SHIPMENT_STATUS_TIMELINE);
                }
            }
            case PICKED_UP -> {
                if (pickedUpAt == null || deliveringAt != null || deliveredAt != null || failedAt != null) {
                    throw new DomainException(DomainErrorCode.INVALID_SHIPMENT_STATUS_TIMELINE);
                }
            }
            case DELIVERING -> {
                if (pickedUpAt == null || deliveringAt == null || deliveredAt != null || failedAt != null) {
                    throw new DomainException(DomainErrorCode.INVALID_SHIPMENT_STATUS_TIMELINE);
                }
            }
            case DELIVERED -> {
                if (pickedUpAt == null || deliveringAt == null || deliveredAt == null || failedAt != null) {
                    throw new DomainException(DomainErrorCode.INVALID_SHIPMENT_STATUS_TIMELINE);
                }
            }
            case FAILED -> {
                if (failedAt == null || deliveredAt != null) {
                    throw new DomainException(DomainErrorCode.INVALID_SHIPMENT_STATUS_TIMELINE);
                }
            }
        }
    }
}
