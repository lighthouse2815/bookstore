ALTER TABLE categories
    ADD COLUMN parent_id BINARY(16) NULL,
    ADD COLUMN image_file_asset_id BINARY(16) NULL,
    ADD KEY idx_categories_parent_id (parent_id),
    ADD KEY idx_categories_image_file_asset_id (image_file_asset_id),
    ADD CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories(id),
    ADD CONSTRAINT fk_categories_image_file_asset
        FOREIGN KEY (image_file_asset_id) REFERENCES file_assets(id);

ALTER TABLE publishers
    ADD COLUMN logo_file_asset_id BINARY(16) NULL,
    ADD KEY idx_publishers_logo_file_asset_id (logo_file_asset_id),
    ADD CONSTRAINT fk_publishers_logo_file_asset
        FOREIGN KEY (logo_file_asset_id) REFERENCES file_assets(id);
