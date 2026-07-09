package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.BookRatingSummaryResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReadingJournalEntryResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.ReadingJournalEntry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadingJournalAssembler {

    private final IBookRepository bookRepository;
    private final IReviewRepository reviewRepository;
    private final IOrderRepository orderRepository;

    public PageSliceResult<ReadingJournalEntryResult> toPageResult(PageSliceResult<ReadingJournalEntry> pageResult) {
        if (pageResult.items().isEmpty()) {
            return new PageSliceResult<>(List.of(), pageResult.totalCount(), pageResult.page(), pageResult.size());
        }

        Map<UUID, BookQueryResult> booksById = enrichBooks(
                pageResult.items().stream()
                        .map(ReadingJournalEntry::getBookId)
                        .toList()
        );

        return new PageSliceResult<>(
                pageResult.items().stream()
                        .map(entry -> toResult(entry, booksById))
                        .toList(),
                pageResult.totalCount(),
                pageResult.page(),
                pageResult.size()
        );
    }

    public ReadingJournalEntryResult toResult(ReadingJournalEntry entry) {
        Map<UUID, BookQueryResult> booksById = enrichBooks(List.of(entry.getBookId()));
        return toResult(entry, booksById);
    }

    private ReadingJournalEntryResult toResult(
            ReadingJournalEntry entry,
            Map<UUID, BookQueryResult> booksById
    ) {
        BookQueryResult book = booksById.get(entry.getBookId());
        if (book == null) {
            throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
        }

        return new ReadingJournalEntryResult(
                entry.getId(),
                entry.getEntryDate(),
                entry.getNote(),
                entry.getCurrentPage(),
                entry.getProgressPercent(),
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                book
        );
    }

    private Map<UUID, BookQueryResult> enrichBooks(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        List<Book> books = bookRepository.findAllByIdsIncludingDeleted(bookIds);
        List<UUID> distinctBookIds = books.stream()
                .map(Book::getId)
                .toList();
        Map<UUID, BookRatingSummaryResult> ratingSummaries = buildRatingSummaries(
                reviewRepository.findRatingsByBookIds(distinctBookIds)
        );
        Map<UUID, Long> soldCounts = orderRepository.countDeliveredQuantityByBookIds(distinctBookIds);

        return books.stream()
                .collect(Collectors.toMap(
                        Book::getId,
                        book -> new BookQueryResult(
                                book,
                                soldCounts.getOrDefault(book.getId(), 0L),
                                ratingSummaries.getOrDefault(book.getId(), emptyRatingSummary())
                        ),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
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
