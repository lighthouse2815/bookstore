-- Generated from current JPA mappings against MySQL 8 baseline schema
SET FOREIGN_KEY_CHECKS = 0;
CREATE TABLE `authors` (
  `birth_year` int DEFAULT NULL,
  `death_year` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `avatar_file_asset_id` binary(16) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `biography` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9mhkwvnfaarcalo4noabrin5j` (`name`),
  KEY `FKseorqa9fu923dfu5i1ghqec0s` (`avatar_file_asset_id`),
  CONSTRAINT `FKseorqa9fu923dfu5i1ghqec0s` FOREIGN KEY (`avatar_file_asset_id`) REFERENCES `file_assets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `book_details` (
  `page_count` int DEFAULT NULL,
  `publication_year` int DEFAULT NULL,
  `weight` int DEFAULT NULL,
  `book_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `cover_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dimensions` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `edition` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `language` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `translator` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcj1y16bigff5oyvo049gj8vsi` (`book_id`),
  CONSTRAINT `FKoa7sqrtgxwg066s9521udtvpv` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `book_images` (
  `primary_image` bit(1) NOT NULL,
  `sort_order` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `book_id` binary(16) NOT NULL,
  `file_asset_id` binary(16) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `alt_text` text COLLATE utf8mb4_unicode_ci,
  `image_url` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `FKcnpy06tjmrsjisjf2bqpuvvbl` (`book_id`),
  KEY `FKbehic41vdxkry0da6vxroikfv` (`file_asset_id`),
  CONSTRAINT `FKbehic41vdxkry0da6vxroikfv` FOREIGN KEY (`file_asset_id`) REFERENCES `file_assets` (`id`),
  CONSTRAINT `FKcnpy06tjmrsjisjf2bqpuvvbl` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `books` (
  `price` decimal(19,2) NOT NULL,
  `stock_quantity` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `author_id` binary(16) NOT NULL,
  `category_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `publisher_id` binary(16) NOT NULL,
  `isbn` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `image_url` text COLLATE utf8mb4_unicode_ci,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfjixh2vym2cvfj3ufxj91jem7` (`author_id`),
  KEY `FKleqa3hhc0uhfvurq6mil47xk0` (`category_id`),
  KEY `FKayy5edfrqnegqj3882nce6qo8` (`publisher_id`),
  CONSTRAINT `FKayy5edfrqnegqj3882nce6qo8` FOREIGN KEY (`publisher_id`) REFERENCES `publishers` (`id`),
  CONSTRAINT `FKfjixh2vym2cvfj3ufxj91jem7` FOREIGN KEY (`author_id`) REFERENCES `authors` (`id`),
  CONSTRAINT `FKleqa3hhc0uhfvurq6mil47xk0` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `cart_items` (
  `quantity` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `book_id` binary(16) DEFAULT NULL,
  `cart_id` binary(16) NOT NULL,
  `digital_asset_id` binary(16) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `item_type` enum('DIGITAL_ASSET','PHYSICAL_BOOK') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_items_cart_book_type` (`cart_id`,`book_id`,`item_type`),
  UNIQUE KEY `uk_cart_items_cart_digital_type` (`cart_id`,`digital_asset_id`,`item_type`),
  KEY `idx_cart_items_cart_id` (`cart_id`),
  KEY `idx_cart_items_digital_asset_id` (`digital_asset_id`),
  KEY `FKhiu1jw80o45wfiw5tgok1xpkl` (`book_id`),
  CONSTRAINT `FKd1fdt3x1hmg71a6ra3buvawvc` FOREIGN KEY (`digital_asset_id`) REFERENCES `digital_assets` (`id`),
  CONSTRAINT `FKhiu1jw80o45wfiw5tgok1xpkl` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`),
  CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `carts` (
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK64t7ox312pqal3p7fg9o503c2` (`user_id`),
  CONSTRAINT `FKb5o626f86h46m4s7ms6ginnop` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `categories` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt8o6pivur7nn124jehx7cygw5` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `chat_conversation_participants` (
  `unread_count` int NOT NULL,
  `joined_at` datetime(6) NOT NULL,
  `last_read_at` datetime(6) DEFAULT NULL,
  `left_at` datetime(6) DEFAULT NULL,
  `conversation_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `last_read_message_id` binary(16) DEFAULT NULL,
  `user_id` binary(16) NOT NULL,
  `role` enum('ADMIN','STAFF','SYSTEM','USER') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_participant_conversation_user` (`conversation_id`,`user_id`),
  KEY `idx_chat_participant_conversation` (`conversation_id`),
  KEY `idx_chat_participant_user` (`user_id`),
  CONSTRAINT `FK36uuu5yq54y1bbwsbws6tdtee` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversations` (`id`),
  CONSTRAINT `FKg8hsuyfu6d755taw6qqelohab` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `chat_conversations` (
  `closed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `last_message_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `assigned_staff_id` binary(16) DEFAULT NULL,
  `customer_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `last_message_id` binary(16) DEFAULT NULL,
  `target_id` binary(16) DEFAULT NULL,
  `last_message_preview` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `subject` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `priority` enum('HIGH','LOW','NORMAL','URGENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CLOSED','OPEN','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` enum('BOOK','GENERAL','ORDER') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_conversation_customer` (`customer_id`),
  KEY `idx_chat_conversation_assigned_staff` (`assigned_staff_id`),
  KEY `idx_chat_conversation_status` (`status`),
  KEY `idx_chat_conversation_last_message_at` (`last_message_at`),
  CONSTRAINT `FKld52p1xmde0cjvgv223ari29b` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKlu1hvh3ecoybcxexevkkqhivb` FOREIGN KEY (`assigned_staff_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `chat_messages` (
  `attachment_size` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `conversation_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `sender_id` binary(16) NOT NULL,
  `attachment_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `attachment_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_type` enum('FILE','IMAGE','SYSTEM','TEXT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender_role` enum('ADMIN','STAFF','SYSTEM','USER') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_message_conversation` (`conversation_id`),
  KEY `idx_chat_message_sender` (`sender_id`),
  KEY `idx_chat_message_created_at` (`created_at`),
  CONSTRAINT `FKgiqeap8ays4lf684x7m0r2729` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqgkanrr90j46564w4ww63jcna` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `coupon_targets` (
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `coupon_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `target_id` binary(16) DEFAULT NULL,
  `target_type` enum('ALL_ORDER','AUTHOR','BOOK','CATEGORY','PUBLISHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3w3e6f5siuh8hym0cj81ejuba` (`coupon_id`),
  CONSTRAINT `FK3w3e6f5siuh8hym0cj81ejuba` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `coupon_usages` (
  `discount_amount` decimal(19,2) NOT NULL,
  `used_at` datetime(6) NOT NULL,
  `coupon_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `order_id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3mvslb8gc0ac6501mfmvifgva` (`coupon_id`),
  KEY `FKs9yuckyrsqcsgmjsus1unapt4` (`order_id`),
  KEY `FK6mev6grxbqmt8l0jxvobfg70n` (`user_id`),
  CONSTRAINT `FK3mvslb8gc0ac6501mfmvifgva` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`),
  CONSTRAINT `FK6mev6grxbqmt8l0jxvobfg70n` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKs9yuckyrsqcsgmjsus1unapt4` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `coupons` (
  `active` bit(1) NOT NULL,
  `discount_value` decimal(19,2) NOT NULL,
  `max_discount_amount` decimal(19,2) DEFAULT NULL,
  `max_usage_count` int DEFAULT NULL,
  `min_order_amount` decimal(19,2) NOT NULL,
  `used_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `starts_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `coupon_type` enum('BOOK','SHIPPING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_type` enum('FIXED_AMOUNT','PERCENTAGE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKeplt0kkm9yf2of2lnx6c1oy9b` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `digital_assets` (
  `download_allowed` bit(1) NOT NULL,
  `price` decimal(19,2) NOT NULL,
  `published` bit(1) NOT NULL,
  `purchase_allowed` bit(1) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `book_id` binary(16) NOT NULL,
  `file_asset_id` binary(16) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `sample_file_asset_id` binary(16) DEFAULT NULL,
  `mime_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `checksum` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sample_storage_key` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `storage_key` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `format` enum('AUDIO','EPUB','PDF') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_digital_assets_book_id` (`book_id`),
  KEY `idx_digital_assets_format` (`format`),
  KEY `idx_digital_assets_published` (`published`),
  KEY `FK96mi7lhosbsowg6nbl9u9ykeu` (`file_asset_id`),
  KEY `FKrm5l215538fcarbglc3fq996h` (`sample_file_asset_id`),
  CONSTRAINT `FK2exbfhktqjl17h4tx94t7y2cw` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`),
  CONSTRAINT `FK96mi7lhosbsowg6nbl9u9ykeu` FOREIGN KEY (`file_asset_id`) REFERENCES `file_assets` (`id`),
  CONSTRAINT `FKrm5l215538fcarbglc3fq996h` FOREIGN KEY (`sample_file_asset_id`) REFERENCES `file_assets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `file_assets` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `size_bytes` bigint DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `created_by` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `content_type` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `checksum_sha256` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `storage_key` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `bucket` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `original_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `public_url` text COLLATE utf8mb4_unicode_ci,
  `provider` enum('R2','S3') COLLATE utf8mb4_unicode_ci NOT NULL,
  `purpose` enum('AUTHOR_AVATAR','BOOK_IMAGE','EBOOK_FILE','INVOICE','REVIEW_IMAGE','SAMPLE_FILE','USER_AVATAR') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','DELETED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `visibility` enum('PRIVATE','PUBLIC') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_file_assets_status` (`status`),
  KEY `idx_file_assets_purpose` (`purpose`),
  KEY `idx_file_assets_storage_key` (`storage_key`),
  KEY `idx_file_assets_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `import_receipt_items` (
  `item_order` int DEFAULT NULL,
  `line_total` decimal(19,2) NOT NULL,
  `quantity` int NOT NULL,
  `unit_cost` decimal(19,2) NOT NULL,
  `book_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `import_receipt_id` binary(16) NOT NULL,
  `book_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjbla7jiqi6hylcvl190ee63cu` (`book_id`),
  KEY `FK514gtw9t9ehyljt1bj0t9h28n` (`import_receipt_id`),
  CONSTRAINT `FK514gtw9t9ehyljt1bj0t9h28n` FOREIGN KEY (`import_receipt_id`) REFERENCES `import_receipts` (`id`),
  CONSTRAINT `FKjbla7jiqi6hylcvl190ee63cu` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `import_receipts` (
  `total_amount` decimal(19,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `created_by` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `FKa1a55hf4b0yva4cdscu0ybhrs` (`supplier_id`),
  CONSTRAINT `FKa1a55hf4b0yva4cdscu0ybhrs` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notifications` (
  `read_flag` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `target_id` binary(16) DEFAULT NULL,
  `user_id` binary(16) NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notification_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9y21adhxn0ayjhfocscqox7bh` (`user_id`),
  CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `order_items` (
  `item_order` int DEFAULT NULL,
  `line_total` decimal(19,2) NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(19,2) NOT NULL,
  `book_id` binary(16) NOT NULL,
  `digital_asset_id` binary(16) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `order_id` binary(16) NOT NULL,
  `book_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_type` enum('DIGITAL_ASSET','PHYSICAL_BOOK') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi4ptndslo2pyfp9r1x0eulh9g` (`book_id`),
  KEY `FK4asb1fj71lsnf2ipwal8ed5av` (`digital_asset_id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  CONSTRAINT `FK4asb1fj71lsnf2ipwal8ed5av` FOREIGN KEY (`digital_asset_id`) REFERENCES `digital_assets` (`id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKi4ptndslo2pyfp9r1x0eulh9g` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `orders` (
  `coupon_discount` decimal(19,2) NOT NULL,
  `discount_amount` decimal(19,2) NOT NULL,
  `final_amount` decimal(19,2) NOT NULL,
  `product_total` decimal(19,2) NOT NULL,
  `shipping_discount` decimal(19,2) NOT NULL,
  `shipping_fee` decimal(19,2) NOT NULL,
  `total_amount` decimal(19,2) NOT NULL,
  `cancelled_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `book_coupon_id` binary(16) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `shipping_coupon_id` binary(16) DEFAULT NULL,
  `user_id` binary(16) NOT NULL,
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `book_coupon_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipping_coupon_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receiver_address` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_method` enum('BANK_TRANSFER','BANK_TRANSFER_QR','CASH','COD') COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_status` enum('CANCELLED','FAILED','PAID','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CANCELLED','CONFIRMED','DELIVERED','PENDING','SHIPPING') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKdhk2umg8ijjkg4njg6891trit` (`order_code`),
  KEY `FKq4vm6gx921kkq6ps4fc9xdw7t` (`book_coupon_id`),
  KEY `FKmeawgcqmrddy5ffbkq8nvn46h` (`shipping_coupon_id`),
  KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKmeawgcqmrddy5ffbkq8nvn46h` FOREIGN KEY (`shipping_coupon_id`) REFERENCES `coupons` (`id`),
  CONSTRAINT `FKq4vm6gx921kkq6ps4fc9xdw7t` FOREIGN KEY (`book_coupon_id`) REFERENCES `coupons` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `password_reset_tokens` (
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `token_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKajre85ybxavf1tt4omkrs5p6g` (`token_hash`),
  KEY `idx_password_reset_tokens_user_created_at` (`user_id`,`created_at`),
  CONSTRAINT `FKk3ndxg5xp6v7wd4gjyusp15gq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `payments` (
  `amount` decimal(19,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `order_id` binary(16) NOT NULL,
  `gateway` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `merchant_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reference_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `transaction_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transfer_content` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider` enum('COD','POS','SEPAY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('CANCELLED','FAILED','PAID','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_payments_order_id` (`order_id`),
  KEY `idx_payments_reference_code` (`reference_code`),
  KEY `idx_payments_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `permissions` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `code` enum('AUTHOR_CREATE','AUTHOR_DELETE','AUTHOR_UPDATE','AUTHOR_VIEW','BOOK_CREATE','BOOK_DELETE','BOOK_UPDATE','BOOK_VIEW','CATEGORY_CREATE','CATEGORY_DELETE','CATEGORY_UPDATE','CATEGORY_VIEW','COUPON_MANAGE','DASHBOARD_VIEW','ORDER_CANCEL_OWN','ORDER_CREATE','ORDER_UPDATE_STATUS','ORDER_VIEW_ALL','ORDER_VIEW_OWN','PUBLISHER_CREATE','PUBLISHER_DELETE','PUBLISHER_UPDATE','PUBLISHER_VIEW','REVIEW_CREATE','REVIEW_MANAGE','ROLE_MANAGE','ROLE_VIEW','SHIPMENT_ASSIGN','SHIPMENT_UPDATE_OWN','SHIPMENT_VIEW_ALL','SHIPMENT_VIEW_OWN','USER_MANAGE','USER_VIEW') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7lcb6glmvwlro3p2w2cewxtvd` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `profiles` (
  `date_of_birth` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `avatar_file_asset_id` binary(16) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `first_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` enum('FEMALE','MALE','OTHER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4ixsj6aqve5pxrbw2u0oyk8bb` (`user_id`),
  KEY `FKl6p1p86y1hh3opbb12a8n8pcg` (`avatar_file_asset_id`),
  CONSTRAINT `FK410q61iev7klncmpqfuo85ivh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl6p1p86y1hh3opbb12a8n8pcg` FOREIGN KEY (`avatar_file_asset_id`) REFERENCES `file_assets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `publishers` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKan1ucpx8sw2qm194mlok8e5us` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `reading_progresses` (
  `current_page` int DEFAULT NULL,
  `progress_percent` decimal(5,2) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `last_read_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `digital_asset_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `position_data` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reading_progresses_user_asset` (`user_id`,`digital_asset_id`),
  KEY `idx_reading_progresses_user_id` (`user_id`),
  KEY `idx_reading_progresses_digital_asset_id` (`digital_asset_id`),
  CONSTRAINT `FKap3jp930wnf8p53d9mixs2i16` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqr5xigbdeydbn5bi11x8ed70u` FOREIGN KEY (`digital_asset_id`) REFERENCES `digital_assets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `refresh_tokens` (
  `revoked` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `token` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKghpmfn23vmxfu3spu3lfg4r2d` (`token`),
  KEY `FK1lih5y2npsf8u5o3vhdb9y0os` (`user_id`),
  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `reviews` (
  `rating` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `book_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `order_item_id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK96f6ovfc9wn4579incehx4gra` (`order_item_id`),
  KEY `FK6a9k6xvev80se5rreqvuqr7f9` (`book_id`),
  KEY `FKcgy7qjc1r99dp117y9en6lxye` (`user_id`),
  CONSTRAINT `FK2x2x74lnliqmt91bc1w95ll8n` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`),
  CONSTRAINT `FK6a9k6xvev80se5rreqvuqr7f9` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`),
  CONSTRAINT `FKcgy7qjc1r99dp117y9en6lxye` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `role_permissions` (
  `permission_id` binary(16) NOT NULL,
  `role_id` binary(16) NOT NULL,
  PRIMARY KEY (`permission_id`,`role_id`),
  KEY `FKn5fotdgk8d1xvo8nav9uv3muc` (`role_id`),
  CONSTRAINT `FKegdk29eiy7mdtefy5c7eirr6e` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `FKn5fotdgk8d1xvo8nav9uv3muc` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `roles` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKofx66keruapi6vyqpv6f2or37` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `shipments` (
  `assigned_at` datetime(6) NOT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `delivering_at` datetime(6) DEFAULT NULL,
  `failed_at` datetime(6) DEFAULT NULL,
  `picked_up_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `order_id` binary(16) NOT NULL,
  `shipper_id` binary(16) NOT NULL,
  `failure_reason` text COLLATE utf8mb4_unicode_ci,
  `status` enum('ASSIGNED','DELIVERED','DELIVERING','FAILED','PICKED_UP') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrnt4wht95lxxplspltrg9681s` (`order_id`),
  KEY `FKedr38oic7jqbdv22wd03a44` (`shipper_id`),
  CONSTRAINT `FKedr38oic7jqbdv22wd03a44` FOREIGN KEY (`shipper_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKrnt4wht95lxxplspltrg9681s` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `stock_movements` (
  `after_quantity` int NOT NULL,
  `before_quantity` int NOT NULL,
  `quantity` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `book_id` binary(16) NOT NULL,
  `created_by` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `reference_id` binary(16) DEFAULT NULL,
  `reference_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `type` enum('ADJUSTMENT','CANCEL_ORDER','IMPORT','SALE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnpbabikpekwh9a8w83em5nkcb` (`book_id`),
  KEY `FKtarfmf9jkv9ovq74yswik0gsl` (`created_by`),
  CONSTRAINT `FKnpbabikpekwh9a8w83em5nkcb` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`),
  CONSTRAINT `FKtarfmf9jkv9ovq74yswik0gsl` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `suppliers` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` text COLLATE utf8mb4_unicode_ci,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKeegixpn11chp14nb25tl3ucv0` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_addresses` (
  `default_address` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_address` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `receiver_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn2fisxyyu3l9wlch3ve2nocgp` (`user_id`),
  CONSTRAINT `FKn2fisxyyu3l9wlch3ve2nocgp` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_auth_identities` (
  `email_verified` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `provider_email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_subject` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider` enum('GOOGLE','LOCAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_auth_identities_provider_subject` (`provider`,`provider_subject`),
  UNIQUE KEY `uk_user_auth_identities_user_provider` (`user_id`,`provider`),
  KEY `idx_user_auth_identities_user_id` (`user_id`),
  CONSTRAINT `FKb842ijimhojgn7bvqalxekuu3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_digital_accesses` (
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `digital_asset_id` binary(16) NOT NULL,
  `id` binary(16) NOT NULL,
  `source_order_id` binary(16) DEFAULT NULL,
  `user_id` binary(16) NOT NULL,
  `access_type` enum('BORROWED','PURCHASED','SUBSCRIPTION') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','EXPIRED','REVOKED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_digital_accesses_user_asset_type` (`user_id`,`digital_asset_id`,`access_type`),
  KEY `idx_user_digital_accesses_user_id` (`user_id`),
  KEY `idx_user_digital_accesses_digital_asset_id` (`digital_asset_id`),
  KEY `idx_user_digital_accesses_status` (`status`),
  CONSTRAINT `FKhnj0vxej43768kll6wih7hdbx` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKnx8ych6f02q7hr8mtat59nfbx` FOREIGN KEY (`digital_asset_id`) REFERENCES `digital_assets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_otps` (
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `invalidated_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  `otp_hash` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `purpose` enum('PASSWORD_RESET','REGISTRATION') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_otps_user_purpose_created_at` (`user_id`,`purpose`,`created_at`),
  CONSTRAINT `FK6b7wl9e3l4ry3m4afynki929` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_roles` (
  `role_id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`role_id`,`user_id`),
  KEY `FKhfh9dx7w3ubf1co1vdev94g3f` (`user_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `locked` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `id` binary(16) NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK9q63snka3mdh91as4io72espi` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
