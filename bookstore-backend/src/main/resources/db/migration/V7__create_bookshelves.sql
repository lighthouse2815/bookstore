CREATE TABLE bookshelves (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bookshelves_user_name UNIQUE (user_id, name),
    CONSTRAINT fk_bookshelves_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_bookshelves_user_id ON bookshelves (user_id);
CREATE INDEX idx_bookshelves_user_deleted_at ON bookshelves (user_id, deleted_at);

CREATE TABLE bookshelf_items (
    id BINARY(16) NOT NULL,
    shelf_id BINARY(16) NOT NULL,
    book_id BINARY(16) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bookshelf_items_shelf_book UNIQUE (shelf_id, book_id),
    CONSTRAINT fk_bookshelf_items_shelf FOREIGN KEY (shelf_id) REFERENCES bookshelves (id),
    CONSTRAINT fk_bookshelf_items_book FOREIGN KEY (book_id) REFERENCES books (id)
);

CREATE INDEX idx_bookshelf_items_shelf_order ON bookshelf_items (shelf_id, deleted_at, sort_order);
CREATE INDEX idx_bookshelf_items_book_id ON bookshelf_items (book_id);
