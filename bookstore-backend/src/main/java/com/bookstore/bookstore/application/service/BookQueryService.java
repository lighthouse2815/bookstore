package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IBookQueryService;
import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.application.port.out.IAuthorRepository;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.BookPageDetailResult;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.BookRatingSummaryResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookQueryService implements IBookQueryService {

    private static final int DEFAULT_RELATED_LIMIT = 8;
    private static final int MAX_RELATED_LIMIT = 20;

    private final IBookRepository bookRepository;
    private final IAuthorRepository authorRepository;
    private final ICategoryRepository categoryRepository;
    private final IPublisherRepository publisherRepository;
    private final IReviewRepository reviewRepository;
    private final IOrderRepository orderRepository;
    private final ICouponService couponService;

    @Override
    @Transactional(readOnly = true)
    public List<BookQueryResult> getAll() {
        return enrichBooks(bookRepository.findAllActive());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookQueryResult> search(String keyword) {
        String normalizedKeyword = StringUtils.trimToNull(keyword);
        if (normalizedKeyword == null) {
            return getAll();
        }
        return enrichBooks(bookRepository.searchByKeywordActive(normalizedKeyword));
    }

    @Override
    @Transactional(readOnly = true)
    public BookQueryResult getById(UUID bookId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }

        Book book = bookRepository.findByIdActive(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        return enrichBook(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookPageDetailResult getPageDetail(UUID bookId, int relatedLimit) {
        BookQueryResult bookResult = getById(bookId);
        Book book = bookResult.book();

        return new BookPageDetailResult(
                bookResult,
                authorRepository.findByIdIncludingDeleted(book.getAuthorId())
                        .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND)),
                publisherRepository.findByIdIncludingDeleted(book.getPublisherId())
                        .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PUBLISHER_NOT_FOUND)),
                resolveCategoryTrail(book.getCategoryId()),
                bookResult.ratingSummary(),
                couponService.getPublicActivePromotions(Instant.now()),
                resolveRelatedBooks(book, relatedLimit)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookQueryResult> getRelatedBooks(UUID bookId, int limit) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }

        Book book = bookRepository.findByIdActive(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        return resolveRelatedBooks(book, limit);
    }

    private BookQueryResult enrichBook(Book book) {
        return enrichBooks(List.of(book)).getFirst();
    }

    private List<BookQueryResult> enrichBooks(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return List.of();
        }

        List<UUID> bookIds = books.stream()
                .map(Book::getId)
                .toList();
        Map<UUID, BookRatingSummaryResult> ratingSummaries = buildRatingSummaries(reviewRepository.findRatingsByBookIds(bookIds));
        Map<UUID, Long> soldCounts = orderRepository.countDeliveredQuantityByBookIds(bookIds);

        return books.stream()
                .map(book -> new BookQueryResult(
                        book,
                        soldCounts.getOrDefault(book.getId(), 0L),
                        ratingSummaries.getOrDefault(book.getId(), emptyRatingSummary())
                ))
                .toList();
    }

    private Map<UUID, BookRatingSummaryResult> buildRatingSummaries(Map<UUID, List<Integer>> ratingsByBookId) {
        if (ratingsByBookId == null || ratingsByBookId.isEmpty()) {
            return Map.of();
        }

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
            final int currentStar = star;
            starBreakdown.put(star, ratings.stream().filter(rating -> rating == currentStar).count());
        }

        BigDecimal averageRating = BigDecimal.valueOf(
                        ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0)
                )
                .setScale(1, RoundingMode.HALF_UP);

        return new BookRatingSummaryResult(averageRating, ratings.size(), Collections.unmodifiableMap(starBreakdown));
    }

    private BookRatingSummaryResult emptyRatingSummary() {
        Map<Integer, Long> starBreakdown = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            starBreakdown.put(star, 0L);
        }
        return new BookRatingSummaryResult(BigDecimal.ZERO.setScale(1), 0L, Collections.unmodifiableMap(starBreakdown));
    }

    private List<Category> resolveCategoryTrail(UUID categoryId) {
        if (categoryId == null) {
            return List.of();
        }

        List<Category> trail = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        UUID currentId = categoryId;
        while (currentId != null && visited.add(currentId)) {
            var category = categoryRepository.findByIdIncludingDeleted(currentId).orElse(null);
            if (category == null) {
                break;
            }
            trail.add(category);
            currentId = category.getParentId();
        }
        Collections.reverse(trail);
        return List.copyOf(trail);
    }

    private List<BookQueryResult> resolveRelatedBooks(Book book, int limit) {
        int normalizedLimit = normalizeRelatedLimit(limit);
        if (normalizedLimit <= 0) {
            return List.of();
        }

        return enrichBooks(bookRepository.findRelatedActiveByCategoryId(book.getCategoryId(), book.getId(), normalizedLimit));
    }

    private int normalizeRelatedLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_RELATED_LIMIT;
        }
        return Math.min(limit, MAX_RELATED_LIMIT);
    }
}
