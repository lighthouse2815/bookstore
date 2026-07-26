CREATE TABLE refunds (
    id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    payment_id BINARY(16) NOT NULL,
    return_request_id BINARY(16) NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(32) NOT NULL,
    external_reference VARCHAR(255) NULL,
    evidence_url VARCHAR(1000) NULL,
    evidence_metadata LONGTEXT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    requested_by BINARY(16) NOT NULL,
    approved_by BINARY(16) NULL,
    processed_by BINARY(16) NULL,
    requested_at DATETIME(6) NOT NULL,
    approved_at DATETIME(6) NULL,
    processed_at DATETIME(6) NULL,
    failure_reason VARCHAR(1000) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refunds_order_idempotency_key UNIQUE (order_id, idempotency_key),
    CONSTRAINT fk_refunds_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_refunds_return_request FOREIGN KEY (return_request_id) REFERENCES return_requests (id),
    CONSTRAINT fk_refunds_requested_by FOREIGN KEY (requested_by) REFERENCES users (id),
    CONSTRAINT fk_refunds_approved_by FOREIGN KEY (approved_by) REFERENCES users (id),
    CONSTRAINT fk_refunds_processed_by FOREIGN KEY (processed_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refunds_payment_status ON refunds (payment_id, status);
CREATE INDEX idx_refunds_status_requested_at ON refunds (status, requested_at);
CREATE INDEX idx_refunds_order_id ON refunds (order_id);
CREATE INDEX idx_refunds_return_request_id ON refunds (return_request_id);

CREATE TABLE outbox_events (
    id BINARY(16) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id BINARY(16) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload LONGTEXT NOT NULL,
    deduplication_key CHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME(6) NOT NULL,
    locked_at DATETIME(6) NULL,
    locked_by VARCHAR(128) NULL,
    last_error VARCHAR(2000) NULL,
    created_at DATETIME(6) NOT NULL,
    processed_at DATETIME(6) NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_outbox_events_deduplication_key UNIQUE (deduplication_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_outbox_claimable ON outbox_events (status, next_attempt_at, created_at);
CREATE INDEX idx_outbox_processing_lock ON outbox_events (status, locked_at);
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_type, aggregate_id);

CREATE TABLE outbox_deliveries (
    id BINARY(16) NOT NULL,
    event_id BINARY(16) NOT NULL,
    consumer VARCHAR(100) NOT NULL,
    delivered_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_outbox_deliveries_event_consumer UNIQUE (event_id, consumer),
    CONSTRAINT fk_outbox_deliveries_event FOREIGN KEY (event_id) REFERENCES outbox_events (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
