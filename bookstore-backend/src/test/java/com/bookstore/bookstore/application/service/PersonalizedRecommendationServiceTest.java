package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IBookshelfRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReadingJournalRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.port.out.IWishlistRepository;
import com.bookstore.bookstore.application.result.PersonalizedRecommendationResult;
import com.bookstore.bookstore.application.result.RecommendationReasonCode;
import com.bookstore.bookstore.application.result.RecommendationStrategy;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.ReviewStatus;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.domain.model.BookshelfItem;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.ReadingJournalEntry;
import com.bookstore.bookstore.domain.model.Review;
import com.bookstore.bookstore.domain.model.WishlistItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalizedRecommendationServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IWishlistRepository wishlistRepository;

    @Mock
    private IBookshelfRepository bookshelfRepository;

    @Mock
    private IReviewRepository reviewRepository;

    @Mock
    private IReadingJournalRepository readingJournalRepository;

    @InjectMocks
    private PersonalizedRecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        lenient().when(orderRepository.findByUserId(any())).thenReturn(List.of());
        lenient().when(wishlistRepository.findAllByUserIdActive(any())).thenReturn(List.of());
        lenient().when(bookshelfRepository.findAllByUserIdActive(any())).thenReturn(List.of());
        lenient().when(bookshelfRepository.findAllItemsByShelfIdsActive(any())).thenReturn(List.of());
        lenient().when(reviewRepository.findAllByUserIdActive(any())).thenReturn(List.of());
        lenient().when(readingJournalRepository.findAllByUserIdActive(any())).thenReturn(List.of());
        lenient().when(reviewRepository.findRatingsByBookIds(any())).thenReturn(Map.of());
        lenient().when(orderRepository.countDeliveredQuantityByBookIds(any())).thenReturn(Map.of());
    }

    @Test
    void getForUser_favorsBooksSharingPurchasedCategoryAndAuthor_andExcludesThePurchasedBook() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Book purchased = book(UUID.randomUUID(), categoryId, authorId, 10);
        Book sameCategory = book(UUID.randomUUID(), categoryId, UUID.randomUUID(), 10);
        Book sameAuthor = book(UUID.randomUUID(), UUID.randomUUID(), authorId, 10);
        Book unrelated = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        when(bookRepository.findAllActive()).thenReturn(List.of(purchased, sameCategory, sameAuthor, unrelated));
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(deliveredOrder(userId, purchased.getId())));

        PersonalizedRecommendationResult result = recommendationService.getForUser(userId, 3);

        assertEquals(RecommendationStrategy.PERSONALIZED, result.strategy());
        assertTrue(result.hasPersonalSignals());
        assertFalse(result.items().stream().anyMatch(item -> item.book().book().getId().equals(purchased.getId())));
        assertTrue(result.items().getFirst().reasonCodes().contains(RecommendationReasonCode.FAVORITE_CATEGORY));
        assertTrue(result.items().getFirst().reasonCodes().contains(RecommendationReasonCode.PURCHASE_HISTORY));
    }

    @Test
    void getForUser_usesWishlistReviewAndJournalSignals_withoutExposingPrivateContent() {
        UUID userId = UUID.randomUUID();
        Book wishlistBook = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        Book reviewBook = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        Book journalBook = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        when(bookRepository.findAllActive()).thenReturn(List.of(wishlistBook, reviewBook, journalBook));
        when(wishlistRepository.findAllByUserIdActive(userId)).thenReturn(List.of(wishlistItem(userId, wishlistBook.getId())));
        when(reviewRepository.findAllByUserIdActive(userId)).thenReturn(List.of(review(userId, reviewBook.getId(), 5)));
        when(readingJournalRepository.findAllByUserIdActive(userId)).thenReturn(List.of(journalEntry(userId, journalBook.getId())));

        PersonalizedRecommendationResult result = recommendationService.getForUser(userId, 3);

        assertTrue(result.items().stream()
                .filter(item -> item.book().book().getId().equals(wishlistBook.getId()))
                .flatMap(item -> item.reasonCodes().stream())
                .anyMatch(reason -> reason == RecommendationReasonCode.WISHLIST_SIGNAL));
        assertTrue(result.items().stream()
                .flatMap(item -> item.reasonCodes().stream())
                .anyMatch(reason -> reason == RecommendationReasonCode.HIGH_RATING_REVIEW));
        assertTrue(result.items().stream()
                .flatMap(item -> item.reasonCodes().stream())
                .anyMatch(reason -> reason == RecommendationReasonCode.READING_JOURNAL_SIGNAL));
    }

    @Test
    void getForUser_newUserFallsBackToPopularAndHighRatedAvailableBooks() {
        UUID userId = UUID.randomUUID();
        Book lowRated = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        Book popular = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        when(bookRepository.findAllActive()).thenReturn(List.of(lowRated, popular));
        when(reviewRepository.findRatingsByBookIds(any())).thenReturn(Map.of(popular.getId(), List.of(5, 5)));
        when(orderRepository.countDeliveredQuantityByBookIds(any())).thenReturn(Map.of(popular.getId(), 12L));

        PersonalizedRecommendationResult result = recommendationService.getForUser(userId, 2);

        assertEquals(RecommendationStrategy.FALLBACK_POPULAR, result.strategy());
        assertFalse(result.hasPersonalSignals());
        assertEquals(popular.getId(), result.items().getFirst().book().book().getId());
        assertTrue(result.items().getFirst().reasonCodes().contains(RecommendationReasonCode.FALLBACK_POPULAR));
    }

    @Test
    void getForUser_filtersOutOfStockBooks_andUsesStableIdTieBreak() {
        UUID userId = UUID.randomUUID();
        UUID firstId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID secondId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        Book first = book(firstId, UUID.randomUUID(), UUID.randomUUID(), 10);
        Book second = book(secondId, UUID.randomUUID(), UUID.randomUUID(), 10);
        Book unavailable = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 0);
        when(bookRepository.findAllActive()).thenReturn(List.of(second, unavailable, first));

        PersonalizedRecommendationResult result = recommendationService.getForUser(userId, 3);

        assertEquals(List.of(firstId, secondId), result.items().stream()
                .map(item -> item.book().book().getId())
                .toList());
    }

    @Test
    void getForUser_appliesSoftCategoryDiversityBeforeFillingRemainingCandidates() {
        UUID userId = UUID.randomUUID();
        UUID crowdedCategory = UUID.randomUUID();
        UUID favoriteAuthor = UUID.randomUUID();
        Book purchased = book(UUID.randomUUID(), crowdedCategory, favoriteAuthor, 10);
        List<Book> crowded = java.util.stream.IntStream.range(0, 5)
                .mapToObj(index -> book(UUID.randomUUID(), crowdedCategory, UUID.randomUUID(), 10))
                .toList();
        List<Book> alternatives = java.util.stream.IntStream.range(0, 2)
                .mapToObj(index -> book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10))
                .toList();
        List<Book> books = new java.util.ArrayList<>();
        books.add(purchased);
        books.addAll(crowded);
        books.addAll(alternatives);
        when(bookRepository.findAllActive()).thenReturn(books);
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(deliveredOrder(userId, purchased.getId())));

        PersonalizedRecommendationResult result = recommendationService.getForUser(userId, 6);

        long selectedFromCrowdedCategory = result.items().stream()
                .filter(item -> item.book().book().getCategoryId().equals(crowdedCategory))
                .count();
        assertEquals(4L, selectedFromCrowdedCategory);
        assertEquals(6, result.items().size());
    }

    @Test
    void getForUser_usesActiveBookshelfAndFourStarReviewSignals() {
        UUID userId = UUID.randomUUID();
        UUID shelfId = UUID.randomUUID();
        Book shelfBook = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        Book reviewedBook = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        when(bookRepository.findAllActive()).thenReturn(List.of(shelfBook, reviewedBook));
        when(bookshelfRepository.findAllByUserIdActive(userId)).thenReturn(List.of(bookshelf(userId, shelfId)));
        when(bookshelfRepository.findAllItemsByShelfIdsActive(List.of(shelfId)))
                .thenReturn(List.of(bookshelfItem(shelfId, shelfBook.getId())));
        when(reviewRepository.findAllByUserIdActive(userId)).thenReturn(List.of(review(userId, reviewedBook.getId(), 4)));

        PersonalizedRecommendationResult result = recommendationService.getForUser(userId, 2);

        assertTrue(reasonCodesFor(result, shelfBook.getId()).contains(RecommendationReasonCode.BOOKSHELF_SIGNAL));
        assertTrue(reasonCodesFor(result, reviewedBook.getId()).contains(RecommendationReasonCode.HIGH_RATING_REVIEW));
    }

    @Test
    void getForUser_ignoresCancelledOrdersForPurchaseSignalsAndExclusion() {
        UUID userId = UUID.randomUUID();
        Book cancelledBook = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        Book otherBook = book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        when(bookRepository.findAllActive()).thenReturn(List.of(cancelledBook, otherBook));
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(cancelledOrder(userId, cancelledBook.getId())));

        PersonalizedRecommendationResult result = recommendationService.getForUser(userId, 2);

        assertFalse(result.hasPersonalSignals());
        assertTrue(result.items().stream().anyMatch(item -> item.book().book().getId().equals(cancelledBook.getId())));
    }

    @Test
    void getForUser_appliesSoftAuthorDiversityBeforeFillingRemainingCandidates() {
        UUID userId = UUID.randomUUID();
        UUID crowdedAuthor = UUID.randomUUID();
        Book purchased = book(UUID.randomUUID(), UUID.randomUUID(), crowdedAuthor, 10);
        List<Book> crowded = java.util.stream.IntStream.range(0, 4)
                .mapToObj(index -> book(UUID.randomUUID(), UUID.randomUUID(), crowdedAuthor, 10))
                .toList();
        List<Book> alternatives = java.util.stream.IntStream.range(0, 2)
                .mapToObj(index -> book(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10))
                .toList();
        List<Book> books = new java.util.ArrayList<>();
        books.add(purchased);
        books.addAll(crowded);
        books.addAll(alternatives);
        when(bookRepository.findAllActive()).thenReturn(books);
        when(orderRepository.findByUserId(userId)).thenReturn(List.of(deliveredOrder(userId, purchased.getId())));

        PersonalizedRecommendationResult result = recommendationService.getForUser(userId, 5);

        long selectedFromCrowdedAuthor = result.items().stream()
                .filter(item -> item.book().book().getAuthorId().equals(crowdedAuthor))
                .count();
        assertEquals(3L, selectedFromCrowdedAuthor);
        assertEquals(5, result.items().size());
    }

    @Test
    void getForUser_rejectsLimitsOutsideThePublicContract() {
        assertThrows(ApplicationException.class, () -> recommendationService.getForUser(UUID.randomUUID(), 0));
        assertThrows(ApplicationException.class, () -> recommendationService.getForUser(UUID.randomUUID(), 25));
    }

    private static List<RecommendationReasonCode> reasonCodesFor(
            PersonalizedRecommendationResult result,
            UUID bookId
    ) {
        return result.items().stream()
                .filter(item -> item.book().book().getId().equals(bookId))
                .findFirst()
                .orElseThrow()
                .reasonCodes();
    }

    private static Book book(UUID id, UUID categoryId, UUID authorId, int stockQuantity) {
        return new Book(
                id,
                "Book " + id,
                "ISBN-" + id,
                null,
                BigDecimal.TEN,
                stockQuantity,
                List.of(),
                null,
                categoryId,
                authorId,
                UUID.randomUUID(),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
    }

    private static Order deliveredOrder(UUID userId, UUID bookId) {
        return order(userId, bookId, OrderStatus.DELIVERED);
    }

    private static Order cancelledOrder(UUID userId, UUID bookId) {
        return order(userId, bookId, OrderStatus.CANCELLED);
    }

    private static Order order(UUID userId, UUID bookId, OrderStatus status) {
        OrderItem item = new OrderItem(UUID.randomUUID(), bookId, "Book", BigDecimal.TEN, 1, BigDecimal.TEN);
        Instant cancelledAt = status == OrderStatus.CANCELLED ? Instant.EPOCH : null;
        return new Order(
                UUID.randomUUID(), userId, List.of(item), BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.TEN, null, null, PaymentMethod.COD, PaymentStatus.PENDING, status,
                "Receiver", "0123456789", "Address", Instant.EPOCH, Instant.EPOCH, cancelledAt
        );
    }

    private static Bookshelf bookshelf(UUID userId, UUID shelfId) {
        return new Bookshelf(shelfId, userId, "Doc sau", Instant.EPOCH, Instant.EPOCH, null);
    }

    private static BookshelfItem bookshelfItem(UUID shelfId, UUID bookId) {
        return new BookshelfItem(UUID.randomUUID(), shelfId, bookId, 0, Instant.EPOCH, Instant.EPOCH, null);
    }

    private static WishlistItem wishlistItem(UUID userId, UUID bookId) {
        return new WishlistItem(UUID.randomUUID(), userId, bookId, Instant.EPOCH, Instant.EPOCH, null);
    }

    private static Review review(UUID userId, UUID bookId, int rating) {
        return new Review(
                UUID.randomUUID(), userId, bookId, UUID.randomUUID(), rating, null, ReviewStatus.APPROVED,
                null, null, null, Instant.EPOCH, Instant.EPOCH, null
        );
    }

    private static ReadingJournalEntry journalEntry(UUID userId, UUID bookId) {
        return new ReadingJournalEntry(
                UUID.randomUUID(), userId, bookId, LocalDate.now().minusDays(1), null, 20,
                BigDecimal.valueOf(20), Instant.EPOCH, Instant.now(), null
        );
    }
}
