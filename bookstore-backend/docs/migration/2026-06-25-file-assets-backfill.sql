-- Manual migration: backfill legacy file columns into file_assets.
-- Project note: this backend does not currently use Flyway/Liquibase, so run this
-- script manually against MySQL 8 after the new columns/tables have been created
-- by Hibernate ddl-auto update or equivalent schema management.
--
-- Assumptions:
-- - UUID columns use Hibernate/MySQL BINARY(16), so this script uses UUID_TO_BIN().
-- - If your database stores UUIDs as CHAR(36), change all BINARY(16) temp columns
--   to CHAR(36) and replace UUID_TO_BIN(UUID()) with UUID().
-- - Set @file_asset_bucket to the active R2/S3 bucket before running.
-- - If old public URLs include a CDN/public base URL, set @legacy_public_url_prefix
--   so storage_key is stored as the object key instead of the whole URL.

SET @file_asset_provider = 'R2';
SET @file_asset_bucket = 'bookstore-assets';
SET @backfill_created_by = UUID_TO_BIN('00000000-0000-0000-0000-000000000000');
SET @legacy_public_url_prefix = NULL;

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_file_asset_backfill;

CREATE TEMPORARY TABLE tmp_file_asset_backfill (
    source_table VARCHAR(64) NOT NULL,
    source_id BINARY(16) NOT NULL,
    source_role VARCHAR(32) NOT NULL,
    file_asset_id BINARY(16) NOT NULL,
    PRIMARY KEY (source_table, source_id, source_role)
);

-- Optional compatibility path: old primary cover stored only on books.image_url.
INSERT INTO tmp_file_asset_backfill (source_table, source_id, source_role, file_asset_id)
SELECT 'books', b.id, 'primary_image', UUID_TO_BIN(UUID())
FROM books b
WHERE b.image_url IS NOT NULL
  AND TRIM(b.image_url) <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM book_images bi
      WHERE bi.book_id = b.id
        AND bi.file_asset_id IS NOT NULL
  );

-- Required path: book_images.image_url -> file_assets PUBLIC.
INSERT INTO tmp_file_asset_backfill (source_table, source_id, source_role, file_asset_id)
SELECT 'book_images', bi.id, 'image', UUID_TO_BIN(UUID())
FROM book_images bi
WHERE bi.file_asset_id IS NULL
  AND bi.image_url IS NOT NULL
  AND TRIM(bi.image_url) <> '';

-- Required path: profiles.avatar_url -> file_assets PUBLIC.
INSERT INTO tmp_file_asset_backfill (source_table, source_id, source_role, file_asset_id)
SELECT 'profiles', p.id, 'avatar', UUID_TO_BIN(UUID())
FROM profiles p
WHERE p.avatar_file_asset_id IS NULL
  AND p.avatar_url IS NOT NULL
  AND TRIM(p.avatar_url) <> '';

-- Required path: authors.avatar_url -> file_assets PUBLIC.
INSERT INTO tmp_file_asset_backfill (source_table, source_id, source_role, file_asset_id)
SELECT 'authors', a.id, 'avatar', UUID_TO_BIN(UUID())
FROM authors a
WHERE a.avatar_file_asset_id IS NULL
  AND a.avatar_url IS NOT NULL
  AND TRIM(a.avatar_url) <> '';

-- Required path: digital_assets.storage_key -> file_assets PRIVATE.
INSERT INTO tmp_file_asset_backfill (source_table, source_id, source_role, file_asset_id)
SELECT 'digital_assets', da.id, 'main', UUID_TO_BIN(UUID())
FROM digital_assets da
WHERE da.file_asset_id IS NULL
  AND da.storage_key IS NOT NULL
  AND TRIM(da.storage_key) <> '';

-- Required path: digital_assets.sample_storage_key -> file_assets PRIVATE.
INSERT INTO tmp_file_asset_backfill (source_table, source_id, source_role, file_asset_id)
SELECT 'digital_assets', da.id, 'sample', UUID_TO_BIN(UUID())
FROM digital_assets da
WHERE da.sample_file_asset_id IS NULL
  AND da.sample_storage_key IS NOT NULL
  AND TRIM(da.sample_storage_key) <> '';

INSERT INTO file_assets (
    id,
    provider,
    purpose,
    bucket,
    storage_key,
    public_url,
    original_name,
    content_type,
    size_bytes,
    checksum_sha256,
    visibility,
    status,
    created_by,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    m.file_asset_id,
    @file_asset_provider,
    'BOOK_IMAGE',
    @file_asset_bucket,
    CASE
        WHEN @legacy_public_url_prefix IS NOT NULL
             AND TRIM(b.image_url) LIKE CONCAT(TRIM(TRAILING '/' FROM @legacy_public_url_prefix), '/%')
        THEN SUBSTRING(TRIM(b.image_url), CHAR_LENGTH(TRIM(TRAILING '/' FROM @legacy_public_url_prefix)) + 2)
        ELSE TRIM(b.image_url)
    END,
    TRIM(b.image_url),
    NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(TRIM(b.image_url), '?', 1), '/', -1), ''),
    CASE LOWER(SUBSTRING_INDEX(SUBSTRING_INDEX(TRIM(b.image_url), '?', 1), '.', -1))
        WHEN 'jpg' THEN 'image/jpeg'
        WHEN 'jpeg' THEN 'image/jpeg'
        WHEN 'png' THEN 'image/png'
        WHEN 'webp' THEN 'image/webp'
        ELSE NULL
    END,
    NULL,
    NULL,
    'PUBLIC',
    'ACTIVE',
    @backfill_created_by,
    COALESCE(b.created_at, CURRENT_TIMESTAMP(6)),
    CURRENT_TIMESTAMP(6),
    NULL
FROM tmp_file_asset_backfill m
JOIN books b ON b.id = m.source_id
WHERE m.source_table = 'books'
  AND m.source_role = 'primary_image';

INSERT INTO file_assets (
    id,
    provider,
    purpose,
    bucket,
    storage_key,
    public_url,
    original_name,
    content_type,
    size_bytes,
    checksum_sha256,
    visibility,
    status,
    created_by,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    m.file_asset_id,
    @file_asset_provider,
    'BOOK_IMAGE',
    @file_asset_bucket,
    CASE
        WHEN @legacy_public_url_prefix IS NOT NULL
             AND TRIM(bi.image_url) LIKE CONCAT(TRIM(TRAILING '/' FROM @legacy_public_url_prefix), '/%')
        THEN SUBSTRING(TRIM(bi.image_url), CHAR_LENGTH(TRIM(TRAILING '/' FROM @legacy_public_url_prefix)) + 2)
        ELSE TRIM(bi.image_url)
    END,
    TRIM(bi.image_url),
    NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(TRIM(bi.image_url), '?', 1), '/', -1), ''),
    CASE LOWER(SUBSTRING_INDEX(SUBSTRING_INDEX(TRIM(bi.image_url), '?', 1), '.', -1))
        WHEN 'jpg' THEN 'image/jpeg'
        WHEN 'jpeg' THEN 'image/jpeg'
        WHEN 'png' THEN 'image/png'
        WHEN 'webp' THEN 'image/webp'
        ELSE NULL
    END,
    NULL,
    NULL,
    'PUBLIC',
    'ACTIVE',
    @backfill_created_by,
    COALESCE(bi.created_at, CURRENT_TIMESTAMP(6)),
    CURRENT_TIMESTAMP(6),
    NULL
FROM tmp_file_asset_backfill m
JOIN book_images bi ON bi.id = m.source_id
WHERE m.source_table = 'book_images'
  AND m.source_role = 'image';

INSERT INTO file_assets (
    id,
    provider,
    purpose,
    bucket,
    storage_key,
    public_url,
    original_name,
    content_type,
    size_bytes,
    checksum_sha256,
    visibility,
    status,
    created_by,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    m.file_asset_id,
    @file_asset_provider,
    'USER_AVATAR',
    @file_asset_bucket,
    CASE
        WHEN @legacy_public_url_prefix IS NOT NULL
             AND TRIM(p.avatar_url) LIKE CONCAT(TRIM(TRAILING '/' FROM @legacy_public_url_prefix), '/%')
        THEN SUBSTRING(TRIM(p.avatar_url), CHAR_LENGTH(TRIM(TRAILING '/' FROM @legacy_public_url_prefix)) + 2)
        ELSE TRIM(p.avatar_url)
    END,
    TRIM(p.avatar_url),
    NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(TRIM(p.avatar_url), '?', 1), '/', -1), ''),
    CASE LOWER(SUBSTRING_INDEX(SUBSTRING_INDEX(TRIM(p.avatar_url), '?', 1), '.', -1))
        WHEN 'jpg' THEN 'image/jpeg'
        WHEN 'jpeg' THEN 'image/jpeg'
        WHEN 'png' THEN 'image/png'
        WHEN 'webp' THEN 'image/webp'
        ELSE NULL
    END,
    NULL,
    NULL,
    'PUBLIC',
    'ACTIVE',
    p.user_id,
    COALESCE(p.created_at, CURRENT_TIMESTAMP(6)),
    CURRENT_TIMESTAMP(6),
    NULL
FROM tmp_file_asset_backfill m
JOIN profiles p ON p.id = m.source_id
WHERE m.source_table = 'profiles'
  AND m.source_role = 'avatar';

INSERT INTO file_assets (
    id,
    provider,
    purpose,
    bucket,
    storage_key,
    public_url,
    original_name,
    content_type,
    size_bytes,
    checksum_sha256,
    visibility,
    status,
    created_by,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    m.file_asset_id,
    @file_asset_provider,
    'AUTHOR_AVATAR',
    @file_asset_bucket,
    CASE
        WHEN @legacy_public_url_prefix IS NOT NULL
             AND TRIM(a.avatar_url) LIKE CONCAT(TRIM(TRAILING '/' FROM @legacy_public_url_prefix), '/%')
        THEN SUBSTRING(TRIM(a.avatar_url), CHAR_LENGTH(TRIM(TRAILING '/' FROM @legacy_public_url_prefix)) + 2)
        ELSE TRIM(a.avatar_url)
    END,
    TRIM(a.avatar_url),
    NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(TRIM(a.avatar_url), '?', 1), '/', -1), ''),
    CASE LOWER(SUBSTRING_INDEX(SUBSTRING_INDEX(TRIM(a.avatar_url), '?', 1), '.', -1))
        WHEN 'jpg' THEN 'image/jpeg'
        WHEN 'jpeg' THEN 'image/jpeg'
        WHEN 'png' THEN 'image/png'
        WHEN 'webp' THEN 'image/webp'
        ELSE NULL
    END,
    NULL,
    NULL,
    'PUBLIC',
    'ACTIVE',
    @backfill_created_by,
    COALESCE(a.created_at, CURRENT_TIMESTAMP(6)),
    CURRENT_TIMESTAMP(6),
    NULL
FROM tmp_file_asset_backfill m
JOIN authors a ON a.id = m.source_id
WHERE m.source_table = 'authors'
  AND m.source_role = 'avatar';

INSERT INTO file_assets (
    id,
    provider,
    purpose,
    bucket,
    storage_key,
    public_url,
    original_name,
    content_type,
    size_bytes,
    checksum_sha256,
    visibility,
    status,
    created_by,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    m.file_asset_id,
    @file_asset_provider,
    'EBOOK_FILE',
    @file_asset_bucket,
    TRIM(da.storage_key),
    NULL,
    COALESCE(NULLIF(TRIM(da.file_name), ''), NULLIF(SUBSTRING_INDEX(TRIM(da.storage_key), '/', -1), '')),
    NULLIF(TRIM(da.mime_type), ''),
    da.file_size,
    NULLIF(TRIM(da.checksum), ''),
    'PRIVATE',
    'ACTIVE',
    @backfill_created_by,
    COALESCE(da.created_at, CURRENT_TIMESTAMP(6)),
    CURRENT_TIMESTAMP(6),
    NULL
FROM tmp_file_asset_backfill m
JOIN digital_assets da ON da.id = m.source_id
WHERE m.source_table = 'digital_assets'
  AND m.source_role = 'main';

INSERT INTO file_assets (
    id,
    provider,
    purpose,
    bucket,
    storage_key,
    public_url,
    original_name,
    content_type,
    size_bytes,
    checksum_sha256,
    visibility,
    status,
    created_by,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    m.file_asset_id,
    @file_asset_provider,
    'SAMPLE_FILE',
    @file_asset_bucket,
    TRIM(da.sample_storage_key),
    NULL,
    NULLIF(SUBSTRING_INDEX(TRIM(da.sample_storage_key), '/', -1), ''),
    NULLIF(TRIM(da.mime_type), ''),
    NULL,
    NULL,
    'PRIVATE',
    'ACTIVE',
    @backfill_created_by,
    COALESCE(da.created_at, CURRENT_TIMESTAMP(6)),
    CURRENT_TIMESTAMP(6),
    NULL
FROM tmp_file_asset_backfill m
JOIN digital_assets da ON da.id = m.source_id
WHERE m.source_table = 'digital_assets'
  AND m.source_role = 'sample';

INSERT INTO book_images (
    id,
    book_id,
    file_asset_id,
    image_url,
    primary_image,
    sort_order,
    alt_text,
    created_at
)
SELECT
    UUID_TO_BIN(UUID()),
    b.id,
    m.file_asset_id,
    NULL,
    TRUE,
    0,
    NULL,
    COALESCE(b.created_at, CURRENT_TIMESTAMP(6))
FROM tmp_file_asset_backfill m
JOIN books b ON b.id = m.source_id
WHERE m.source_table = 'books'
  AND m.source_role = 'primary_image';

UPDATE book_images bi
JOIN tmp_file_asset_backfill m
  ON m.source_table = 'book_images'
 AND m.source_id = bi.id
 AND m.source_role = 'image'
SET bi.file_asset_id = m.file_asset_id
WHERE bi.file_asset_id IS NULL;

UPDATE profiles p
JOIN tmp_file_asset_backfill m
  ON m.source_table = 'profiles'
 AND m.source_id = p.id
 AND m.source_role = 'avatar'
SET p.avatar_file_asset_id = m.file_asset_id
WHERE p.avatar_file_asset_id IS NULL;

UPDATE authors a
JOIN tmp_file_asset_backfill m
  ON m.source_table = 'authors'
 AND m.source_id = a.id
 AND m.source_role = 'avatar'
SET a.avatar_file_asset_id = m.file_asset_id
WHERE a.avatar_file_asset_id IS NULL;

UPDATE digital_assets da
JOIN tmp_file_asset_backfill m
  ON m.source_table = 'digital_assets'
 AND m.source_id = da.id
 AND m.source_role = 'main'
SET da.file_asset_id = m.file_asset_id
WHERE da.file_asset_id IS NULL;

UPDATE digital_assets da
JOIN tmp_file_asset_backfill m
  ON m.source_table = 'digital_assets'
 AND m.source_id = da.id
 AND m.source_role = 'sample'
SET da.sample_file_asset_id = m.file_asset_id
WHERE da.sample_file_asset_id IS NULL;

COMMIT;

-- Post-run checks. All result counts should be 0 before removing or ignoring
-- legacy columns in application reports.
SELECT COUNT(*) AS missing_book_image_assets
FROM book_images
WHERE file_asset_id IS NULL
  AND image_url IS NOT NULL
  AND TRIM(image_url) <> '';

SELECT COUNT(*) AS missing_profile_avatar_assets
FROM profiles
WHERE avatar_file_asset_id IS NULL
  AND avatar_url IS NOT NULL
  AND TRIM(avatar_url) <> '';

SELECT COUNT(*) AS missing_author_avatar_assets
FROM authors
WHERE avatar_file_asset_id IS NULL
  AND avatar_url IS NOT NULL
  AND TRIM(avatar_url) <> '';

SELECT COUNT(*) AS missing_digital_main_assets
FROM digital_assets
WHERE file_asset_id IS NULL
  AND storage_key IS NOT NULL
  AND TRIM(storage_key) <> '';

SELECT COUNT(*) AS missing_digital_sample_assets
FROM digital_assets
WHERE sample_file_asset_id IS NULL
  AND sample_storage_key IS NOT NULL
  AND TRIM(sample_storage_key) <> '';
