ALTER TABLE orders
    ADD COLUMN idempotency_key VARCHAR(64) NULL AFTER cancelled_at,
    ADD COLUMN checkout_fingerprint CHAR(64) NULL AFTER idempotency_key;

CREATE UNIQUE INDEX uk_orders_user_idempotency_key
    ON orders (user_id, idempotency_key);
