CREATE TABLE return_requests (
    id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(50) NOT NULL,
    admin_note VARCHAR(1000) NULL,
    requested_refund_amount DECIMAL(15,2) NULL,
    approved_refund_amount DECIMAL(15,2) NULL,
    processed_by BINARY(16) NULL,
    processed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_return_requests_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_return_requests_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_return_requests_processed_by FOREIGN KEY (processed_by) REFERENCES users (id)
);

CREATE INDEX idx_return_requests_order_id ON return_requests (order_id);
CREATE INDEX idx_return_requests_user_id ON return_requests (user_id);
CREATE INDEX idx_return_requests_status ON return_requests (status);
CREATE INDEX idx_return_requests_created_at ON return_requests (created_at);
