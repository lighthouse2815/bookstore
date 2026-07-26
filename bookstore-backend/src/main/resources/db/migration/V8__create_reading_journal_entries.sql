CREATE TABLE reading_journal_entries (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    book_id BINARY(16) NOT NULL,
    entry_date DATE NOT NULL,
    note VARCHAR(2000) NULL,
    current_page INT NULL,
    progress_percent DECIMAL(5, 2) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reading_journal_user_book_date UNIQUE (user_id, book_id, entry_date),
    CONSTRAINT fk_reading_journal_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reading_journal_book FOREIGN KEY (book_id) REFERENCES books (id)
);

CREATE INDEX idx_reading_journal_user_date ON reading_journal_entries (user_id, deleted_at, entry_date);
CREATE INDEX idx_reading_journal_book_id ON reading_journal_entries (book_id);
