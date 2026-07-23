ALTER TABLE payment_reconciliation_issues
    MODIFY COLUMN deduplication_key VARCHAR(64) NOT NULL;

ALTER TABLE outbox_events
    MODIFY COLUMN deduplication_key VARCHAR(64) NOT NULL;

ALTER TABLE refunds
    MODIFY COLUMN currency VARCHAR(3) NOT NULL;
