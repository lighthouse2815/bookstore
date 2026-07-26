package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IWishlistService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.port.out.IWishlistRepository;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.BookRatingSummaryResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.WishlistItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService implements IWishlistService {

    private final IWishlistRepository wishlistRepository;
    private final IBookRepository bookRepository;
    private final IReviewRepository reviewRepository;
    private final IOrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookQueryResult> getMyWishlist(UUID userId) {
        validateUserId(userId);
        List<UUID> bookIds = wishlistRepository.findAllByUserIdActive(userId).stream()
                .map(WishlistItem::getBookId)
                .toList();
        return enrichBooks(bookRepository.findAllByIdsActive(bookIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBook(UUID userId, UUID bookId) {
        validateUserId(userId);
        validateBookId(bookId);
        bookRepository.findByIdActive(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        var existingItem = wishlistRepository.findByUserIdAndBookId(userId, bookId).orElse(null);
        if (existingItem != null) {
            if (existingItem.isDeleted()) {
                existingItem.restore();
                wishlistRepository.save(existingItem);
            }
            return;
        }

        Instant now = Instant.now();
        wishlistRepository.save(new WishlistItem(
                UUID.randomUUID(),
                userId,
                bookId,
                now,
                now,
                null
        ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBook(UUID userId, UUID bookId) {
        validateUserId(userId);
        validateBookId(bookId);
        wishlistRepository.findByUserIdAndBookId(userId, bookId)
                .filter(item -> !item.isDeleted())
                .ifPresent(item -> {
                    item.softDelete();
                    wishlistRepository.save(item);
                });
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
    }

    private void validateBookId(UUID bookId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
    }

    private List<BookQueryResult> enrichBooks(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return List.of();
        }

        List<UUID> bookIds = books.stream()
                .map(Book::getId)
                .toList();
        Map<UUID, BookRatingSummaryResult> ratingSummaries = buildRatingSummaries(
                reviewRepository.findRatingsByBookIds(bookIds)
        );
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

        return new BookRatingSummaryResult(
                averageRating,
                ratings.size(),
                Collections.unmodifiableMap(starBreakdown)
        );
    }

    private BookRatingSummaryResult emptyRatingSummary() {
        Map<Integer, Long> starBreakdown = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            starBreakdown.put(star, 0L);
        }
        return new BookRatingSummaryResult(
                BigDecimal.ZERO.setScale(1),
                0L,
                Collections.unmodifiableMap(starBreakdown)
        );
    }
}
