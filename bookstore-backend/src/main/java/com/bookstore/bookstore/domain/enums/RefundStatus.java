package com.bookstore.bookstore.domain.enums;

public enum RefundStatus {
    REQUESTED,
    APPROVED,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED;

    public boolean canTransitionTo(RefundStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case REQUESTED -> target == APPROVED || target == CANCELLED;
            case APPROVED -> target == PROCESSING || target == CANCELLED;
            case PROCESSING -> target == SUCCEEDED || target == FAILED;
            case FAILED -> target == PROCESSING || target == CANCELLED;
            case SUCCEEDED, CANCELLED -> false;
        };
    }
}
