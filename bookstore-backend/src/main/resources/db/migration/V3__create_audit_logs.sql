CREATE TABLE audit_logs (
    id BINARY(16) PRIMARY KEY,
    actor_id BINARY(16) NULL,
    actor_username VARCHAR(100) NULL,
    actor_role VARCHAR(50) NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id VARCHAR(100) NULL,
    description VARCHAR(500) NULL,
    before_value LONGTEXT NULL,
    after_value LONGTEXT NULL,
    ip_address VARCHAR(100) NULL,
    user_agent VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL
);

CREATE INDEX idx_audit_logs_actor_id ON audit_logs (actor_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_target_type ON audit_logs (target_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
