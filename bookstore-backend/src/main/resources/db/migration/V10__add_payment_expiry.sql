ALTER TABLE payments
    MODIFY COLUMN status ENUM('CANCELLED', 'EXPIRED', 'FAILED', 'PAID', 'PENDING') NOT NULL,
    ADD COLUMN expires_at DATETIME(6) NULL AFTER paid_at,
    ADD COLUMN expired_at DATETIME(6) NULL AFTER expires_at;

CREATE INDEX idx_payments_pending_expires_at
    ON payments (status, expires_at);
