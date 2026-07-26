ALTER TABLE reviews
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' AFTER comment,
    ADD COLUMN moderation_reason VARCHAR(500) NULL AFTER status,
    ADD COLUMN moderated_by BINARY(16) NULL AFTER moderation_reason,
    ADD COLUMN moderated_at DATETIME(6) NULL AFTER moderated_by;

CREATE INDEX idx_reviews_status ON reviews (status);
