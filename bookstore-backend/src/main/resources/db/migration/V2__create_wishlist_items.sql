CREATE TABLE `wishlist_items` (
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `book_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wishlist_items_user_book` (`user_id`,`book_id`),
  KEY `idx_wishlist_items_user_id` (`user_id`),
  KEY `idx_wishlist_items_book_id` (`book_id`),
  CONSTRAINT `fk_wishlist_items_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_wishlist_items_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
