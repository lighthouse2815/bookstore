package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.enums.ShipmentStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.rule.ShipmentRule;
import com.bookstore.bookstore.domain.validation.Guard;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Shipment {

    private UUID id;
    private UUID orderId;
    private UUID shipperId;
    private ShipmentStatus status;
    private String failureReason;
    private Instant assignedAt;
    private Instant updatedAt;
    private Instant pickedUpAt;
    private Instant deliveringAt;
    private Instant deliveredAt;
    private Instant failedAt;

    public Shipment(
            UUID id,
            UUID orderId,
            UUID shipperId,
            ShipmentStatus status,
            String failureReason,
            Instant assignedAt,
            Instant updatedAt,
            Instant pickedUpAt,
            Instant deliveringAt,
            Instant deliveredAt,
            Instant failedAt
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_SHIPMENT_ID, "id");
        setOrderId(orderId);
        setShipperId(shipperId);
        setStatus(status);
        setFailureReason(failureReason);
        setAssignedAt(assignedAt);
        setUpdatedAt(updatedAt);
        setPickedUpAt(pickedUpAt);
        setDeliveringAt(deliveringAt);
        setDeliveredAt(deliveredAt);
        setFailedAt(failedAt);
        ShipmentRule.requireFailureReasonConsistent(this.status, this.failureReason);
        ShipmentRule.requireStatusTimelineConsistent(
                this.status,
                this.pickedUpAt,
                this.deliveringAt,
                this.deliveredAt,
                this.failedAt
        );
    }

    public void markPickedUp() {
        ShipmentRule.requireCanMarkPickedUp(status);
        ShipmentRule.requireStatusChanged(status, ShipmentStatus.PICKED_UP);
        Instant now = Instant.now();
        setStatus(ShipmentStatus.PICKED_UP);
        setUpdatedAt(now);
        setPickedUpAt(now);
        ShipmentRule.requireStatusTimelineConsistent(status, pickedUpAt, deliveringAt, deliveredAt, failedAt);
    }

    public void startDelivering() {
        ShipmentRule.requireCanStartDelivering(status);
        ShipmentRule.requireStatusChanged(status, ShipmentStatus.DELIVERING);
        Instant now = Instant.now();
        setStatus(ShipmentStatus.DELIVERING);
        setUpdatedAt(now);
        setDeliveringAt(now);
        ShipmentRule.requireStatusTimelineConsistent(status, pickedUpAt, deliveringAt, deliveredAt, failedAt);
    }

    public void markDelivered() {
        ShipmentRule.requireCanMarkDelivered(status);
        ShipmentRule.requireStatusChanged(status, ShipmentStatus.DELIVERED);
        Instant now = Instant.now();
        setStatus(ShipmentStatus.DELIVERED);
        setFailureReason(null);
        setUpdatedAt(now);
        setDeliveredAt(now);
        ShipmentRule.requireStatusTimelineConsistent(status, pickedUpAt, deliveringAt, deliveredAt, failedAt);
    }

    public void markFailed(String failureReason) {
        ShipmentRule.requireCanMarkFailed(status);
        ShipmentRule.requireStatusChanged(status, ShipmentStatus.FAILED);
        Instant now = Instant.now();
        setFailureReason(failureReason);
        setStatus(ShipmentStatus.FAILED);
        setUpdatedAt(now);
        setFailedAt(now);
        ShipmentRule.requireFailureReasonConsistent(status, this.failureReason);
        ShipmentRule.requireStatusTimelineConsistent(status, pickedUpAt, deliveringAt, deliveredAt, failedAt);
    }

    public boolean isOngoing() {
        return status != ShipmentStatus.DELIVERED && status != ShipmentStatus.FAILED;
    }

    private void setOrderId(UUID orderId) {
        this.orderId = Guard.notNull(orderId, DomainErrorCode.INVALID_SHIPMENT_ORDER_ID, "orderId");
    }

    private void setShipperId(UUID shipperId) {
        this.shipperId = Guard.notNull(shipperId, DomainErrorCode.INVALID_SHIPMENT_SHIPPER_ID, "shipperId");
    }

    private void setStatus(ShipmentStatus status) {
        this.status = Guard.notNull(status, DomainErrorCode.INVALID_SHIPMENT_STATUS, "status");
    }

    private void setFailureReason(String failureReason) {
        this.failureReason = Guard.notBlankOrNull(
                failureReason,
                DomainErrorCode.INVALID_SHIPMENT_FAILURE_REASON,
                "failureReason"
        );
    }

    private void setAssignedAt(Instant assignedAt) {
        this.assignedAt = Guard.notInFuture(
                assignedAt,
                DomainErrorCode.INVALID_SHIPMENT_ASSIGNED_AT,
                "assignedAt"
        );
    }

    private void setUpdatedAt(Instant updatedAt) {
        Instant validUpdatedAt = Guard.notInFutureOrNull(
                updatedAt,
                DomainErrorCode.INVALID_SHIPMENT_UPDATED_AT,
                "updatedAt"
        );
        Guard.notBefore(
                validUpdatedAt,
                this.assignedAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "updatedAt",
                "assignedAt"
        );
        this.updatedAt = validUpdatedAt;
    }

    private void setPickedUpAt(Instant pickedUpAt) {
        Instant validPickedUpAt = Guard.notInFutureOrNull(
                pickedUpAt,
                DomainErrorCode.INVALID_SHIPMENT_PICKED_UP_AT,
                "pickedUpAt"
        );
        Guard.notBefore(
                validPickedUpAt,
                this.assignedAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "pickedUpAt",
                "assignedAt"
        );
        this.pickedUpAt = validPickedUpAt;
    }

    private void setDeliveringAt(Instant deliveringAt) {
        Instant validDeliveringAt = Guard.notInFutureOrNull(
                deliveringAt,
                DomainErrorCode.INVALID_SHIPMENT_DELIVERING_AT,
                "deliveringAt"
        );
        Guard.notBefore(
                validDeliveringAt,
                this.assignedAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "deliveringAt",
                "assignedAt"
        );
        Guard.notBefore(
                validDeliveringAt,
                this.pickedUpAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "deliveringAt",
                "pickedUpAt"
        );
        this.deliveringAt = validDeliveringAt;
    }

    private void setDeliveredAt(Instant deliveredAt) {
        Instant validDeliveredAt = Guard.notInFutureOrNull(
                deliveredAt,
                DomainErrorCode.INVALID_SHIPMENT_DELIVERED_AT,
                "deliveredAt"
        );
        Guard.notBefore(
                validDeliveredAt,
                this.assignedAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "deliveredAt",
                "assignedAt"
        );
        Guard.notBefore(
                validDeliveredAt,
                this.pickedUpAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "deliveredAt",
                "pickedUpAt"
        );
        Guard.notBefore(
                validDeliveredAt,
                this.deliveringAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "deliveredAt",
                "deliveringAt"
        );
        this.deliveredAt = validDeliveredAt;
    }

    private void setFailedAt(Instant failedAt) {
        Instant validFailedAt = Guard.notInFutureOrNull(
                failedAt,
                DomainErrorCode.INVALID_SHIPMENT_FAILED_AT,
                "failedAt"
        );
        Guard.notBefore(
                validFailedAt,
                this.assignedAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "failedAt",
                "assignedAt"
        );
        Guard.notBefore(
                validFailedAt,
                this.pickedUpAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "failedAt",
                "pickedUpAt"
        );
        Guard.notBefore(
                validFailedAt,
                this.deliveringAt,
                DomainErrorCode.INVALID_SHIPMENT_AUDIT_ORDER,
                "failedAt",
                "deliveringAt"
        );
        this.failedAt = validFailedAt;
    }
}
