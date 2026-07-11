CREATE TABLE payment_reconciliation_issues (
    id BINARY(16) NOT NULL,
    payment_id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    issue_type VARCHAR(64) NOT NULL,
    expected_amount DECIMAL(19,2) NOT NULL,
    received_amount DECIMAL(19,2) NOT NULL,
    external_transaction_id VARCHAR(100) NULL,
    deduplication_key CHAR(64) NOT NULL,
    details TEXT NULL,
    status VARCHAR(32) NOT NULL,
    detected_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6) NULL,
    resolved_by BINARY(16) NULL,
    resolution_note VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payment_reconciliation_deduplication_key UNIQUE (deduplication_key),
    CONSTRAINT fk_payment_reconciliation_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
    CONSTRAINT fk_payment_reconciliation_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_payment_reconciliation_resolved_by FOREIGN KEY (resolved_by) REFERENCES users (id)
);

CREATE INDEX idx_payment_reconciliation_status_detected
    ON payment_reconciliation_issues (status, detected_at);
CREATE INDEX idx_payment_reconciliation_payment_id
    ON payment_reconciliation_issues (payment_id);
CREATE INDEX idx_payment_reconciliation_order_id
    ON payment_reconciliation_issues (order_id);
