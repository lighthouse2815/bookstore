CREATE TABLE order_timeline_events (
    id BINARY(16) PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    actor_id BINARY(16) NULL,
    actor_name VARCHAR(150) NULL,
    actor_role VARCHAR(50) NULL,
    event_type VARCHAR(100) NOT NULL,
    old_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NULL,
    metadata LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_order_timeline_events_order_id
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_order_timeline_order_id ON order_timeline_events (order_id);
CREATE INDEX idx_order_timeline_event_type ON order_timeline_events (event_type);
CREATE INDEX idx_order_timeline_created_at ON order_timeline_events (created_at);
