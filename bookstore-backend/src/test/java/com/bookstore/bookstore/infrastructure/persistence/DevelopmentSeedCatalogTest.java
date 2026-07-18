package com.bookstore.bookstore.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class DevelopmentSeedCatalogTest {

    @Test
    void catalogContainsFiftyUniqueBooksWithStableCloudCoverKeys() {
        assertEquals(50, DevelopmentSeedCatalog.BOOKS.size());
        assertEquals(50, DevelopmentSeedCatalog.BOOKS.stream()
                .map(DevelopmentSeedCatalog.BookSeed::isbn)
                .distinct()
                .count());

        DevelopmentSeedCatalog.BOOKS.forEach(book -> {
            assertEquals("public/seed/books/%s.jpg".formatted(book.isbn()), book.coverStorageKey());
            assertTrue(book.categoryIndex() >= 0);
            assertTrue(book.categoryIndex() < DevelopmentSeedCatalog.CATEGORIES.size());
            assertTrue(book.publisherIndex() >= 0);
            assertTrue(book.publisherIndex() < DevelopmentSeedCatalog.PUBLISHERS.size());

            String normalizedTitle = book.title().toLowerCase(Locale.ROOT);
            assertFalse(normalizedTitle.matches(".*(sách hay|danh mục|tác giả)\\s*\\d+.*"));
        });
    }

    @Test
    void referenceCatalogSupportsConfigurableCategoryCountsAndRichReferenceData() {
        assertTrue(DevelopmentSeedCatalog.CATEGORIES.size() >= 12);
        assertTrue(DevelopmentSeedCatalog.MIN_CATEGORY_COUNT >= 6);
        assertEquals(49, DevelopmentSeedCatalog.PEOPLE.size());
        assertEquals(50, DevelopmentSeedCatalog.AUTHORS.size());
        assertEquals(50, DevelopmentSeedCatalog.PUBLISHERS.size());
        assertEquals(50, DevelopmentSeedCatalog.SUPPLIERS.size());

        DevelopmentSeedCatalog.AUTHORS.forEach(author -> {
            String normalizedName = author.name().toLowerCase(Locale.ROOT);
            assertFalse(normalizedName.matches(".*(tác giả|author)\\s*\\d+.*"));
            assertTrue(author.biography() != null && !author.biography().isBlank());
        });

        String avatarUrl = DevelopmentSeedCatalog.profileAvatarUrlAt(0);
        assertTrue(avatarUrl.startsWith("https://randomuser.me/api/portraits/"));
        assertTrue(DevelopmentSeedCatalog.supplierNoteAt(0).contains("SáchVui"));
    }
}
