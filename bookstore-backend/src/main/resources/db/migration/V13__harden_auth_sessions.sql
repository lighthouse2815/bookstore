-- Auth/session security hardening. Existing refresh-token values are already SHA-256
-- hashes in application code, so they can safely be retained after the column rename.
ALTER TABLE refresh_tokens
    CHANGE COLUMN token token_hash VARCHAR(64) NOT NULL,
    ADD COLUMN family_id BINARY(16) NULL AFTER user_id,
    ADD COLUMN parent_token_id BINARY(16) NULL AFTER family_id,
    ADD COLUMN replaced_by_token_id BINARY(16) NULL AFTER parent_token_id,
    ADD COLUMN device_id VARCHAR(128) NULL AFTER replaced_by_token_id,
    ADD COLUMN device_name VARCHAR(160) NULL AFTER device_id,
    ADD COLUMN user_agent VARCHAR(500) NULL AFTER device_name,
    ADD COLUMN ip_address VARCHAR(64) NULL AFTER user_agent,
    ADD COLUMN issued_at DATETIME(6) NULL AFTER ip_address,
    ADD COLUMN last_used_at DATETIME(6) NULL AFTER issued_at,
    ADD COLUMN revoked_at DATETIME(6) NULL AFTER last_used_at,
    ADD COLUMN revoke_reason VARCHAR(40) NULL AFTER revoked_at;

-- Each legacy token becomes the root of its own family. This preserves valid sessions
-- while making their next rotation fully family-aware.
UPDATE refresh_tokens
SET family_id = id,
    issued_at = created_at,
    last_used_at = created_at,
    revoked_at = CASE WHEN revoked = b'1' THEN created_at ELSE NULL END,
    revoke_reason = CASE WHEN revoked = b'1' THEN 'LEGACY_REVOKED' ELSE NULL END
WHERE family_id IS NULL;

ALTER TABLE refresh_tokens
    MODIFY COLUMN family_id BINARY(16) NOT NULL,
    MODIFY COLUMN issued_at DATETIME(6) NOT NULL,
    ADD CONSTRAINT fk_refresh_tokens_parent
        FOREIGN KEY (parent_token_id) REFERENCES refresh_tokens(id),
    ADD CONSTRAINT fk_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_tokens(id),
    ADD KEY idx_refresh_tokens_family_id (family_id),
    ADD KEY idx_refresh_tokens_expires_at (expires_at),
    ADD KEY idx_refresh_tokens_active_sessions (user_id, revoked, expires_at, last_used_at);

ALTER TABLE password_reset_tokens
    ADD COLUMN request_ip VARCHAR(64) NULL AFTER token_hash;

ALTER TABLE user_otps
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0 AFTER otp_hash,
    ADD COLUMN max_attempts INT NOT NULL DEFAULT 5 AFTER attempt_count,
    ADD COLUMN last_attempt_at DATETIME(6) NULL AFTER max_attempts;

CREATE TABLE auth_login_attempts (
    id BINARY(16) NOT NULL,
    attempt_type VARCHAR(32) NOT NULL,
    subject_hash VARCHAR(64) NOT NULL,
    failure_count INT NOT NULL,
    window_started_at DATETIME(6) NOT NULL,
    last_failed_at DATETIME(6) NOT NULL,
    locked_until DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_login_attempts_type_subject (attempt_type, subject_hash),
    KEY idx_auth_login_attempts_locked_until (locked_until),
    KEY idx_auth_login_attempts_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
