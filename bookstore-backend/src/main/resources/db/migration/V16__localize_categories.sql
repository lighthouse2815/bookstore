ALTER TABLE categories
    ADD COLUMN code VARCHAR(80) NULL AFTER id;

UPDATE categories
SET code = CASE name
    WHEN 'Văn học' THEN 'LITERATURE'
    WHEN 'Kỹ năng & phát triển bản thân' THEN 'PERSONAL_DEVELOPMENT'
    WHEN 'Thiếu nhi' THEN 'CHILDREN'
    WHEN 'Kinh doanh & quản trị' THEN 'BUSINESS_MANAGEMENT'
    WHEN 'Khoa học & công nghệ' THEN 'SCIENCE_TECHNOLOGY'
    WHEN 'Lịch sử & hồi ký' THEN 'HISTORY_MEMOIR'
    WHEN 'Giả tưởng & kỳ ảo' THEN 'FANTASY'
    WHEN 'Trinh thám' THEN 'MYSTERY'
    WHEN 'Tâm lý học' THEN 'PSYCHOLOGY'
    WHEN 'Triết học' THEN 'PHILOSOPHY'
    WHEN 'Văn học đương đại' THEN 'CONTEMPORARY_LITERATURE'
    WHEN 'Khoa học viễn tưởng' THEN 'SCIENCE_FICTION'
    WHEN 'Giáo dục' THEN 'EDUCATION'
    WHEN 'Nghệ thuật & sáng tạo' THEN 'ART_CREATIVITY'
    WHEN 'Du ký & khám phá' THEN 'TRAVEL_EXPLORATION'
    WHEN 'Ẩm thực & phong cách sống' THEN 'FOOD_LIFESTYLE'
    ELSE CONCAT('CATEGORY_', HEX(id))
END;

ALTER TABLE categories
    MODIFY COLUMN code VARCHAR(80) NOT NULL,
    ADD CONSTRAINT uk_categories_code UNIQUE (code);

CREATE TABLE category_translations (
    id BINARY(16) NOT NULL,
    category_id BINARY(16) NOT NULL,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_category_translations_category_locale UNIQUE (category_id, locale),
    CONSTRAINT fk_category_translations_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO category_translations (id, category_id, locale, name, description)
SELECT UUID_TO_BIN(UUID()), id, 'vi', name, description
FROM categories;

INSERT INTO category_translations (id, category_id, locale, name, description)
SELECT
    UUID_TO_BIN(UUID()),
    id,
    'en',
    CASE name
        WHEN 'Văn học' THEN 'Literature'
        WHEN 'Kỹ năng & phát triển bản thân' THEN 'Skills & Personal Development'
        WHEN 'Thiếu nhi' THEN 'Children''s Books'
        WHEN 'Kinh doanh & quản trị' THEN 'Business & Management'
        WHEN 'Khoa học & công nghệ' THEN 'Science & Technology'
        WHEN 'Lịch sử & hồi ký' THEN 'History & Memoir'
        WHEN 'Giả tưởng & kỳ ảo' THEN 'Fantasy'
        WHEN 'Trinh thám' THEN 'Mystery & Crime'
        WHEN 'Tâm lý học' THEN 'Psychology'
        WHEN 'Triết học' THEN 'Philosophy'
        WHEN 'Văn học đương đại' THEN 'Contemporary Literature'
        WHEN 'Khoa học viễn tưởng' THEN 'Science Fiction'
        WHEN 'Giáo dục' THEN 'Education'
        WHEN 'Nghệ thuật & sáng tạo' THEN 'Art & Creativity'
        WHEN 'Du ký & khám phá' THEN 'Travel & Exploration'
        WHEN 'Ẩm thực & phong cách sống' THEN 'Food & Lifestyle'
        ELSE name
    END,
    CASE name
        WHEN 'Văn học' THEN 'Novels, short stories, and notable literary works.'
        WHEN 'Kỹ năng & phát triển bản thân' THEN 'Practical books for improving mindset, communication, and daily habits.'
        WHEN 'Thiếu nhi' THEN 'Imaginative books for children and families.'
        WHEN 'Kinh doanh & quản trị' THEN 'Books on leadership, operations, finance, and entrepreneurship.'
        WHEN 'Khoa học & công nghệ' THEN 'Accessible science, technology, and discoveries about the natural world.'
        WHEN 'Lịch sử & hồi ký' THEN 'True stories, personal memories, and accounts of historical periods.'
        WHEN 'Giả tưởng & kỳ ảo' THEN 'Magical worlds, mythology, and extraordinary adventures.'
        WHEN 'Trinh thám' THEN 'Cases, mysteries, and journeys in search of the truth.'
        WHEN 'Tâm lý học' THEN 'Explore emotions, behavior, and how people make decisions.'
        WHEN 'Triết học' THEN 'Classic ideas and fundamental questions about life.'
        WHEN 'Văn học đương đại' THEN 'Modern works reflecting people and society today.'
        WHEN 'Khoa học viễn tưởng' THEN 'The future, space, and the impact of science on humanity.'
        WHEN 'Giáo dục' THEN 'Learning resources and educational methods for different age groups.'
        WHEN 'Nghệ thuật & sáng tạo' THEN 'Art, design, photography, and creative practice.'
        WHEN 'Du ký & khám phá' THEN 'Journeys, destinations, and cultural experiences around the world.'
        WHEN 'Ẩm thực & phong cách sống' THEN 'Cooking, home care, and a balanced way of living.'
        ELSE description
    END
FROM categories;
