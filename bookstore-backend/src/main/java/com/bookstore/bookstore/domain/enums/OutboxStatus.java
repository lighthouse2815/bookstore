package com.bookstore.bookstore.domain.enums;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    DEAD
}
