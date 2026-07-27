package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IPersonalizedRecommendationService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IBookshelfRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReadingJournalRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.port.out.IWishlistRepository;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.BookRatingSummaryResult;
import com.bookstore.bookstore.application.result.PersonalizedRecommendationResult;
import com.bookstore.bookstore.application.result.RecommendationReasonCode;
import com.bookstore.bookstore.application.result.RecommendationStrategy;
import com.bookstore.bookstore.application.result.RecommendedBookResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.domain.model.ReadingJournalEntry;
import com.bookstore.bookstore.domain.model.Review;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonalizedRecommendationService implements IPersonalizedRecommendationService {

    private static final int DEFAULT_MAX_RESULTS = 24;
    private static final int FAVORITE_CATEGORY_WEIGHT = 30;
    private static final int FAVORITE_AUTHOR_WEIGHT = 25;
    private static final int PURCHASE_HISTORY_WEIGHT = 20;
    private static final int HIGH_RATING_REVIEW_WEIGHT = 20;
    private static final int WISHLIST_WEIGHT = 15;
    private static final int BOOKSHELF_WEIGHT = 15;
    private static final int JOURNAL_WEIGHT = 12;
    private static final int RECENT_JOURNAL_BONUS = 3;
    private static final int HIGH_RATING_WEIGHT = 8;
    private static final int POPULAR_WEIGHT = 6;
    private static final int NEW_RELEASE_WEIGHT = 4;
    private static final int MAX_BOOKS_PER_CATEGORY = 4;
    private static final int MAX_BOOKS_PER_AUTHOR = 3;
    private static final Instant NEW_RELEASE_CUTOFF = Instant.now().minus(365, ChronoUnit.DAYS);
    private static final Instant RECENT_JOURNAL_CUTOFF = Instant.now().minus(90, ChronoUnit.DAYS);

    private static final List<RecommendationReasonCode> REASON_PRIORITY = List.of(
            RecommendationReasonCode.FAVORITE_AUTHOR,
            RecommendationReasonCode.FAVORITE_CATEGORY,
            RecommendationReasonCode.PURCHASE_HISTORY,
            RecommendationReasonCode.HIGH_RATING_REVIEW,
            RecommendationReasonCode.BOOKSHELF_SIGNAL,
            RecommendationReasonCode.WISHLIST_SIGNAL,
            RecommendationReasonCode.READING_JOURNAL_SIGNAL,
            RecommendationReasonCode.HIGH_RATING,
            RecommendationReasonCode.POPULAR_PICK,
            RecommendationReasonCode.NEW_RELEASE,
            RecommendationReasonCode.FALLBACK_POPULAR
    );

    private final IBookRepository bookRepository;
    private final IOrderRepository orderRepository;
    private final IWishlistRepository wishlistRepository;
    private final IBookshelfRepository bookshelfRepository;
    private final IReviewRepository reviewRepository;
    private final IReadingJournalRepository readingJournalRepository;

    @Override
    @Transactional(readOnly = true)
    public PersonalizedRecommendationResult getForUser(UUID userId, int limit) {
        validateRequest(userId, limit);

        List<Book> allActiveBooks = bookRepository.findAllActive();
        Map<UUID, Book> booksById = allActiveBooks.stream()
                .collect(java.util.stream.Collectors.toMap(Book::getId, book -> book));
        PersonalSignals signals = loadSignals(userId, booksById);
        List<Book> candidates = allActiveBooks.stream()
                .filter(this::isPurchasable)
                .filter(book -> !signals.purchasedBookIds().contains(book.getId()))
                .toList();

        if (candidates.isEmpty()) {
            return new PersonalizedRecommendationResult(
                    List.of(),
                    signals.hasSignals() ? RecommendationStrategy.PERSONALIZED : RecommendationStrategy.FALLBACK_POPULAR,
                    signals.hasSignals(),
                    Instant.now()
            );
        }

        List<UUID> candidateIds = candidates.stream().map(Book::getId).toList();
        Map<UUID, BookRatingSummaryResult> ratings = buildRatingSummaries(
                reviewRepository.findRatingsByBookIds(candidateIds)
        );
        Map<UUID, Long> soldCounts = orderRepository.countDeliveredQuantityByBookIds(candidateIds);
        List<ScoredCandidate> scored = candidates.stream()
                .map(book -> score(book, ratings.getOrDefault(book.getId(), emptyRatingSummary()),
                        soldCounts.getOrDefault(book.getId(), 0L), signals))
                .sorted(candidateComparator())
                .toList();

        List<ScoredCandidate> selected = selectWithDiversity(scored, limit);
        RecommendationStrategy strategy = signals.hasSignals()
                ? RecommendationStrategy.PERSONALIZED
                : RecommendationStrategy.FALLBACK_POPULAR;

        return new PersonalizedRecommendationResult(
                selected.stream().map(ScoredCandidate::toResult).toList(),
                strategy,
                signals.hasSignals(),
                Instant.now()
        );
    }

    private PersonalSignals loadSignals(UUID userId, Map<UUID, Book> booksById) {
        Set<UUID> purchasedBookIds = new HashSet<>();
        orderRepository.findByUserId(userId).stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .map(item -> item.getBookId())
                .filter(booksById::containsKey)
                .forEach(purchasedBookIds::add);

        Set<UUID> wishlistBookIds = activeBookIds(
                wishlistRepository.findAllByUserIdActive(userId).stream().map(item -> item.getBookId()).toList(),
                booksById
        );
        List<Bookshelf> shelves = bookshelfRepository.findAllByUserIdActive(userId);
        Set<UUID> shelfBookIds = activeBookIds(
                bookshelfRepository.findAllItemsByShelfIdsActive(shelves.stream().map(Bookshelf::getId).toList()).stream()
                        .map(item -> item.getBookId())
                        .toList(),
                booksById
        );
        Set<UUID> highRatedReviewBookIds = activeBookIds(
                reviewRepository.findAllByUserIdActive(userId).stream()
                        .filter(review -> review.getRating() >= 4)
                        .map(Review::getBookId)
                        .toList(),
                booksById
        );
        List<ReadingJournalEntry> journalEntries = readingJournalRepository.findAllByUserIdActive(userId);
        Set<UUID> journalBookIds = activeBookIds(
                journalEntries.stream().map(ReadingJournalEntry::getBookId).toList(),
                booksById
        );
        Set<UUID> recentJournalBookIds = activeBookIds(
                journalEntries.stream()
                        .filter(entry -> entry.getUpdatedAt() != null && !entry.getUpdatedAt().isBefore(RECENT_JOURNAL_CUTOFF))
                        .map(ReadingJournalEntry::getBookId)
                        .toList(),
                booksById
        );

        Set<UUID> allSignalBookIds = new HashSet<>();
        allSignalBookIds.addAll(purchasedBookIds);
        allSignalBookIds.addAll(wishlistBookIds);
        allSignalBookIds.addAll(shelfBookIds);
        allSignalBookIds.addAll(highRatedReviewBookIds);
        allSignalBookIds.addAll(journalBookIds);

        return new PersonalSignals(
                purchasedBookIds,
                wishlistBookIds,
                shelfBookIds,
                highRatedReviewBookIds,
                journalBookIds,
                recentJournalBookIds,
                collectCategoryIds(allSignalBookIds, booksById),
                collectAuthorIds(allSignalBookIds, booksById),
                collectCategoryIds(purchasedBookIds, booksById),
                collectAuthorIds(purchasedBookIds, booksById),
                collectCategoryIds(highRatedReviewBookIds, booksById),
                collectAuthorIds(highRatedReviewBookIds, booksById),
                collectCategoryIds(journalBookIds, booksById),
                collectAuthorIds(journalBookIds, booksById)
        );
    }

    private Set<UUID> activeBookIds(Collection<UUID> bookIds, Map<UUID, Book> booksById) {
        return bookIds.stream().filter(booksById::containsKey).collect(java.util.stream.Collectors.toSet());
    }

    private Set<UUID> collectCategoryIds(Collection<UUID> bookIds, Map<UUID, Book> booksById) {
        return bookIds.stream().map(booksById::get).filter(java.util.Objects::nonNull)
                .map(Book::getCategoryId).collect(java.util.stream.Collectors.toSet());
    }

    private Set<UUID> collectAuthorIds(Collection<UUID> bookIds, Map<UUID, Book> booksById) {
        return bookIds.stream().map(booksById::get).filter(java.util.Objects::nonNull)
                .map(Book::getAuthorId).collect(java.util.stream.Collectors.toSet());
    }

    private ScoredCandidate score(
            Book book,
            BookRatingSummaryResult ratingSummary,
            long soldCount,
            PersonalSignals signals
    ) {
        int score = 0;
        Set<RecommendationReasonCode> reasons = new HashSet<>();

        if (signals.favoriteCategoryIds().contains(book.getCategoryId())) {
            score += FAVORITE_CATEGORY_WEIGHT;
            reasons.add(RecommendationReasonCode.FAVORITE_CATEGORY);
        }
        if (signals.favoriteAuthorIds().contains(book.getAuthorId())) {
            score += FAVORITE_AUTHOR_WEIGHT;
            reasons.add(RecommendationReasonCode.FAVORITE_AUTHOR);
        }
        if (signals.purchasedCategoryIds().contains(book.getCategoryId())
                || signals.purchasedAuthorIds().contains(book.getAuthorId())) {
            score += PURCHASE_HISTORY_WEIGHT;
            reasons.add(RecommendationReasonCode.PURCHASE_HISTORY);
        }
        if (signals.highRatedReviewBookIds().contains(book.getId())
                || signals.reviewCategoryIds().contains(book.getCategoryId())
                || signals.reviewAuthorIds().contains(book.getAuthorId())) {
            score += HIGH_RATING_REVIEW_WEIGHT;
            reasons.add(RecommendationReasonCode.HIGH_RATING_REVIEW);
        }
        if (signals.wishlistBookIds().contains(book.getId())) {
            score += WISHLIST_WEIGHT;
            reasons.add(RecommendationReasonCode.WISHLIST_SIGNAL);
        }
        if (signals.shelfBookIds().contains(book.getId())) {
            score += BOOKSHELF_WEIGHT;
            reasons.add(RecommendationReasonCode.BOOKSHELF_SIGNAL);
        }
        if (signals.journalBookIds().contains(book.getId())
                || signals.journalCategoryIds().contains(book.getCategoryId())
                || signals.journalAuthorIds().contains(book.getAuthorId())) {
            score += JOURNAL_WEIGHT;
            if (signals.recentJournalBookIds().contains(book.getId())) {
                score += RECENT_JOURNAL_BONUS;
            }
            reasons.add(RecommendationReasonCode.READING_JOURNAL_SIGNAL);
        }
        if (ratingSummary.averageRating().compareTo(BigDecimal.valueOf(4.5)) >= 0) {
            score += HIGH_RATING_WEIGHT;
            reasons.add(RecommendationReasonCode.HIGH_RATING);
        }
        if (soldCount > 0) {
            score += POPULAR_WEIGHT;
            reasons.add(RecommendationReasonCode.POPULAR_PICK);
        }
        if (!book.getCreatedAt().isBefore(NEW_RELEASE_CUTOFF)) {
            score += NEW_RELEASE_WEIGHT;
            reasons.add(RecommendationReasonCode.NEW_RELEASE);
        }
        if (!signals.hasSignals()) {
            reasons.add(RecommendationReasonCode.FALLBACK_POPULAR);
        }

        return new ScoredCandidate(book, ratingSummary, soldCount, score, orderedReasons(reasons));
    }

    private List<ScoredCandidate> selectWithDiversity(List<ScoredCandidate> candidates, int limit) {
        List<ScoredCandidate> selected = new ArrayList<>();
        Map<UUID, Integer> categoryCounts = new HashMap<>();
        Map<UUID, Integer> authorCounts = new HashMap<>();

        for (ScoredCandidate candidate : candidates) {
            if (selected.size() == limit) {
                return selected;
            }
            if (categoryCounts.getOrDefault(candidate.book().getCategoryId(), 0) >= MAX_BOOKS_PER_CATEGORY
                    || authorCounts.getOrDefault(candidate.book().getAuthorId(), 0) >= MAX_BOOKS_PER_AUTHOR) {
                continue;
            }
            addSelected(selected, categoryCounts, authorCounts, candidate);
        }

        for (ScoredCandidate candidate : candidates) {
            if (selected.size() == limit) {
                break;
            }
            if (!selected.contains(candidate)) {
                addSelected(selected, categoryCounts, authorCounts, candidate);
            }
        }
        return selected;
    }

    private void addSelected(
            List<ScoredCandidate> selected,
            Map<UUID, Integer> categoryCounts,
            Map<UUID, Integer> authorCounts,
            ScoredCandidate candidate
    ) {
        selected.add(candidate);
        categoryCounts.merge(candidate.book().getCategoryId(), 1, Integer::sum);
        authorCounts.merge(candidate.book().getAuthorId(), 1, Integer::sum);
    }

    private Comparator<ScoredCandidate> candidateComparator() {
        return Comparator.comparingInt(ScoredCandidate::score).reversed()
                .thenComparing(candidate -> candidate.ratingSummary().averageRating(), Comparator.reverseOrder())
                .thenComparing(Comparator.comparingLong(ScoredCandidate::soldCount).reversed())
                .thenComparing(candidate -> candidate.book().getCreatedAt(), Comparator.reverseOrder())
                .thenComparing(candidate -> candidate.book().getId());
    }

    private List<RecommendationReasonCode> orderedReasons(Set<RecommendationReasonCode> reasons) {
        return REASON_PRIORITY.stream().filter(reasons::contains).limit(3).toList();
    }

    private Map<UUID, BookRatingSummaryResult> buildRatingSummaries(Map<UUID, List<Integer>> ratingsByBookId) {
        Map<UUID, BookRatingSummaryResult> summaries = new HashMap<>();
        ratingsByBookId.forEach((bookId, ratings) -> summaries.put(bookId, toRatingSummary(ratings)));
        return summaries;
    }

    private BookRatingSummaryResult toRatingSummary(List<Integer> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return emptyRatingSummary();
        }
        Map<Integer, Long> starBreakdown = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            int currentStar = star;
            starBreakdown.put(star, ratings.stream().filter(rating -> rating == currentStar).count());
        }
        BigDecimal average = BigDecimal.valueOf(ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0))
                .setScale(1, RoundingMode.HALF_UP);
        return new BookRatingSummaryResult(average, ratings.size(), Map.copyOf(starBreakdown));
    }

    private BookRatingSummaryResult emptyRatingSummary() {
        Map<Integer, Long> starBreakdown = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            starBreakdown.put(star, 0L);
        }
        return new BookRatingSummaryResult(BigDecimal.ZERO.setScale(1), 0L, Map.copyOf(starBreakdown));
    }

    private boolean isPurchasable(Book book) {
        return book.getDeletedAt() == null && book.getStockQuantity() > 0;
    }

    private void validateRequest(UUID userId, int limit) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (limit < 1 || limit > DEFAULT_MAX_RESULTS) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "limit");
        }
    }

    private record PersonalSignals(
            Set<UUID> purchasedBookIds,
            Set<UUID> wishlistBookIds,
            Set<UUID> shelfBookIds,
            Set<UUID> highRatedReviewBookIds,
            Set<UUID> journalBookIds,
            Set<UUID> recentJournalBookIds,
            Set<UUID> favoriteCategoryIds,
            Set<UUID> favoriteAuthorIds,
            Set<UUID> purchasedCategoryIds,
            Set<UUID> purchasedAuthorIds,
            Set<UUID> reviewCategoryIds,
            Set<UUID> reviewAuthorIds,
            Set<UUID> journalCategoryIds,
            Set<UUID> journalAuthorIds
    ) {
        boolean hasSignals() {
            return !favoriteCategoryIds.isEmpty() || !favoriteAuthorIds.isEmpty();
        }
    }

    private record ScoredCandidate(
            Book book,
            BookRatingSummaryResult ratingSummary,
            long soldCount,
            int score,
            List<RecommendationReasonCode> reasonCodes
    ) {
        RecommendedBookResult toResult() {
            return new RecommendedBookResult(new BookQueryResult(book, soldCount, ratingSummary), reasonCodes);
        }
    }
}
