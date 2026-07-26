package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.BookRatingSummaryResult;
import com.bookstore.bookstore.application.result.BookshelfItemResult;
import com.bookstore.bookstore.application.result.BookshelfResult;
import com.bookstore.bookstore.application.result.BookshelfSummaryResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.domain.model.BookshelfItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookshelfAssembler {

    private final IBookRepository bookRepository;
    private final IReviewRepository reviewRepository;
    private final IOrderRepository orderRepository;

    public List<BookshelfSummaryResult> toSummaryResults(List<Bookshelf> bookshelves, Map<UUID, Long> bookCounts) {
        if (bookshelves == null || bookshelves.isEmpty()) {
            return List.of();
        }

        return bookshelves.stream()
                .map(bookshelf -> toSummaryResult(bookshelf, bookCounts.getOrDefault(bookshelf.getId(), 0L)))
                .toList();
    }

    public BookshelfSummaryResult toSummaryResult(Bookshelf bookshelf, long bookCount) {
        return new BookshelfSummaryResult(
                bookshelf.getId(),
                bookshelf.getName(),
                bookCount,
                bookshelf.getCreatedAt(),
                bookshelf.getUpdatedAt()
        );
    }

    public BookshelfResult toResult(Bookshelf bookshelf, List<BookshelfItem> items) {
        List<BookshelfItem> activeItems = items == null
                ? List.of()
                : items.stream()
                        .filter(item -> !item.isDeleted())
                        .sorted(java.util.Comparator.comparingInt(BookshelfItem::getSortOrder)
                                .thenComparing(BookshelfItem::getCreatedAt)
                                .thenComparing(BookshelfItem::getId))
                        .toList();

        Map<UUID, BookQueryResult> booksById = enrichBooks(
                activeItems.stream()
                        .map(BookshelfItem::getBookId)
                        .toList()
        );

        List<BookshelfItemResult> itemResults = activeItems.stream()
                .map(item -> toItemResult(item, booksById))
                .toList();

        return new BookshelfResult(
                bookshelf.getId(),
                bookshelf.getName(),
                itemResults.size(),
                itemResults,
                bookshelf.getCreatedAt(),
                bookshelf.getUpdatedAt()
        );
    }

    private BookshelfItemResult toItemResult(BookshelfItem item, Map<UUID, BookQueryResult> booksById) {
        BookQueryResult book = booksById.get(item.getBookId());
        if (book == null) {
            throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
        }

        return new BookshelfItemResult(
                item.getId(),
                item.getSortOrder(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
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
