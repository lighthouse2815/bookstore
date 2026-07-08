package com.bookstore.bookstore.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.AuthorJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.WishlistItemJpaEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WishlistItemJpaRepositoryTest {

    @Autowired
    private WishlistItemJpaRepository wishlistItemJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFlush_whenSameUserWishlistsSameBookTwice_throwsUniqueConstraintViolation() {
        UserJpaEntity user = persistUser("wishlist-user");
        BookJpaEntity book = persistBook("wishlist-book");

        wishlistItemJpaRepository.saveAndFlush(wishlistItem(user, book, Instant.parse("2026-01-01T08:00:00Z"), null));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> wishlistItemJpaRepository.saveAndFlush(
                        wishlistItem(user, book, Instant.parse("2026-01-01T08:05:00Z"), null)
                )
        );
    }

    @Test
    void findAllByUserIdActive_excludesSoftDeletedItemsAndOrdersNewestFirst() {
        UserJpaEntity user = persistUser("active-wishlist-user");
        BookJpaEntity olderBook = persistBook("older-book");
        BookJpaEntity newerBook = persistBook("newer-book");

        wishlistItemJpaRepository.saveAndFlush(wishlistItem(
                user,
                olderBook,
                Instant.parse("2026-01-01T08:00:00Z"),
                null
        ));
        wishlistItemJpaRepository.saveAndFlush(wishlistItem(
                user,
                newerBook,
                Instant.parse("2026-01-01T08:10:00Z"),
                null
        ));
        wishlistItemJpaRepository.saveAndFlush(wishlistItem(
                user,
                persistBook("deleted-book"),
                Instant.parse("2026-01-01T08:20:00Z"),
                Instant.parse("2026-01-01T08:30:00Z")
        ));

        List<WishlistItemJpaEntity> result = wishlistItemJpaRepository.findAllByUserIdActive(user.getId());

        assertEquals(2, result.size());
        assertEquals(newerBook.getId(), result.get(0).getBook().getId());
        assertEquals(olderBook.getId(), result.get(1).getBook().getId());
    }

    private UserJpaEntity persistUser(String suffix) {
        Instant now = Instant.parse("2026-01-01T08:00:00Z");
        UserJpaEntity user = new UserJpaEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(suffix);
        user.setPasswordHash("hash");
        user.setPhoneNumber(null);
        user.setEmail(suffix + "@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setLocked(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeletedAt(null);
        entityManager.persist(user);
        return user;
    }

    private BookJpaEntity persistBook(String suffix) {
        Instant now = Instant.parse("2026-01-01T08:00:00Z");
        CategoryJpaEntity category = new CategoryJpaEntity();
        category.setId(UUID.randomUUID());
        category.setName("category-" + suffix);
        category.setDescription("Category " + suffix);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        category.setDeletedAt(null);
        entityManager.persist(category);

        AuthorJpaEntity author = new AuthorJpaEntity();
        author.setId(UUID.randomUUID());
        author.setName("author-" + suffix);
        author.setBiography("Author " + suffix);
        author.setAvatarUrl(null);
        author.setAvatarFileAsset(null);
        author.setBirthYear(1980);
        author.setDeathYear(null);
        author.setCreatedAt(now);
        author.setUpdatedAt(now);
        author.setDeletedAt(null);
        entityManager.persist(author);

        PublisherJpaEntity publisher = new PublisherJpaEntity();
        publisher.setId(UUID.randomUUID());
        publisher.setName("publisher-" + suffix);
        publisher.setDescription("Publisher " + suffix);
        publisher.setCreatedAt(now);
        publisher.setUpdatedAt(now);
        publisher.setDeletedAt(null);
        entityManager.persist(publisher);

        BookJpaEntity book = new BookJpaEntity();
        book.setId(UUID.randomUUID());
        book.setTitle("Book " + suffix);
        book.setIsbn("ISBN-" + suffix);
        book.setDescription("Description " + suffix);
        book.setPrice(new BigDecimal("120000.00"));
        book.setStockQuantity(10);
        book.setImageUrl(null);
        book.setCategory(category);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setCreatedAt(now);
        book.setUpdatedAt(now);
        book.setDeletedAt(null);
        entityManager.persist(book);
        return book;
    }

    private WishlistItemJpaEntity wishlistItem(
            UserJpaEntity user,
            BookJpaEntity book,
            Instant createdAt,
            Instant deletedAt
    ) {
        WishlistItemJpaEntity entity = new WishlistItemJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setUser(user);
        entity.setBook(book);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        entity.setDeletedAt(deletedAt);
        return entity;
    }
}
