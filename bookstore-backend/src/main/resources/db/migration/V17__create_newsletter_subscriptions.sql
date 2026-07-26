CREATE TABLE newsletter_subscriptions (
    id BINARY(16) NOT NULL,
    email VARCHAR(320) NOT NULL,
    status ENUM('ACTIVE', 'UNSUBSCRIBED') NOT NULL,
    unsubscribe_token VARCHAR(36) NOT NULL,
    subscribed_at DATETIME(6) NOT NULL,
    unsubscribed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_newsletter_subscriptions_email (email),
    UNIQUE KEY uk_newsletter_subscriptions_unsubscribe_token (unsubscribe_token),
    KEY idx_newsletter_subscriptions_status (status),
    KEY idx_newsletter_subscriptions_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
