package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.BookDetailCommand;
import com.bookstore.bookstore.application.command.BookImageCommand;
import com.bookstore.bookstore.application.command.CreateBookCommand;
import com.bookstore.bookstore.application.command.DeleteBookCommand;
import com.bookstore.bookstore.application.command.UpdateBookCommand;
import com.bookstore.bookstore.application.result.BookPageDetailResult;
import com.bookstore.bookstore.application.result.BookQueryResult;
import com.bookstore.bookstore.application.result.BookRatingSummaryResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookDetail;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.presentation.request.BookDetailRequest;
import com.bookstore.bookstore.presentation.request.BookImageRequest;
import com.bookstore.bookstore.presentation.request.CreateBookRequest;
import com.bookstore.bookstore.presentation.request.UpdateBookRequest;
import com.bookstore.bookstore.presentation.response.BookDetailResponse;
import com.bookstore.bookstore.presentation.response.BookImageResponse;
import com.bookstore.bookstore.presentation.response.BookPageDetailResponse;
import com.bookstore.bookstore.presentation.response.BookResponse;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookWebMapper {

    private final AuthorWebMapper authorWebMapper;
    private final CouponWebMapper couponWebMapper;
    private final CategoryWebMapper categoryWebMapper;

    public CreateBookCommand toCreateCommand(CreateBookRequest request) {
        return new CreateBookCommand(
                request.title(),
                request.isbn(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                toBookImageCommands(request.images()),
                toBookDetailCommand(request.detail()),
                request.categoryId(),
                request.authorId(),
                request.publisherId()
        );
    }

    public UpdateBookCommand toUpdateCommand(UUID bookId, UpdateBookRequest request) {
        return new UpdateBookCommand(
                bookId,
                request.title(),
                request.isbn(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                toBookImageCommands(request.images()),
                toBookDetailCommand(request.detail()),
                request.categoryId(),
                request.authorId(),
                request.publisherId()
        );
    }

    public DeleteBookCommand toDeleteCommand(UUID bookId) {
        return new DeleteBookCommand(bookId);
    }

    public BookResponse toBookResponse(Book book) {
        return toBookResponse(book, 0L, emptyRatingSummary());
    }

    public BookResponse toBookResponse(BookQueryResult result) {
        return toBookResponse(result.book(), result.soldCount(), result.ratingSummary());
    }

    public BookPageDetailResponse toBookPageDetailResponse(BookPageDetailResult result) {
        BookQueryResult bookResult = result.book();
        return new BookPageDetailResponse(
                new BookPageDetailResponse.DetailBookResponse(
                        bookResult.book().getId(),
                        bookResult.book().getTitle(),
                        bookResult.book().getIsbn(),
                        bookResult.book().getPrice(),
                        null,
                        null,
                        bookResult.book().getStockQuantity(),
                        bookResult.soldCount(),
                        bookResult.book().getDescription(),
                        bookResult.book().getImages().stream().map(this::toBookImageResponse).toList(),
                        toBookDetailResponse(bookResult.book().getDetail()),
                        bookResult.ratingSummary().averageRating(),
                        bookResult.ratingSummary().reviewCount()
                ),
                authorWebMapper.toAuthorResponse(result.author()),
                new BookPageDetailResponse.PublisherSummaryResponse(
                        result.publisher().getId(),
                        result.publisher().getName()
                ),
                result.categoryTrail().stream()
                        .map(category -> new BookPageDetailResponse.CategoryTrailItemResponse(
                                category.getId(),
                                category.getCode(),
                                category.getName(),
                                categoryWebMapper.toTranslationResponses(category)
                        ))
                        .toList(),
                new BookPageDetailResponse.RatingSummaryResponse(
                        result.ratingSummary().averageRating(),
                        result.ratingSummary().reviewCount(),
                        result.ratingSummary().starBreakdown()
                ),
                result.promotions().stream()
                        .map(couponWebMapper::toResponse)
                        .toList(),
                result.relatedBooks().stream()
                        .map(this::toBookResponse)
                        .toList()
        );
    }

    private BookResponse toBookResponse(Book book, long soldCount, BookRatingSummaryResult ratingSummary) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getDescription(),
                book.getPrice(),
                book.getStockQuantity(),
                soldCount,
                ratingSummary.averageRating(),
                ratingSummary.reviewCount(),
                ratingSummary.starBreakdown(),
                book.getPrimaryImageUrl(),
                book.getImages().stream()
                        .map(this::toBookImageResponse)
                        .toList(),
                toBookDetailResponse(book.getDetail()),
                book.getCategoryId(),
                book.getAuthorId(),
                book.getPublisherId(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }

    private BookRatingSummaryResult emptyRatingSummary() {
        Map<Integer, Long> starBreakdown = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            starBreakdown.put(star, 0L);
        }
        return new BookRatingSummaryResult(BigDecimal.ZERO.setScale(1), 0L, Map.copyOf(starBreakdown));
    }

    private List<BookImageCommand> toBookImageCommands(List<BookImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .map(this::toBookImageCommand)
                .toList();
    }

    private BookImageCommand toBookImageCommand(BookImageRequest request) {
        if (request == null) {
            return null;
        }

        return new BookImageCommand(
                request.id(),
                request.fileAssetId(),
                request.primaryImage(),
                request.sortOrder(),
                request.altText()
        );
    }

    private BookDetailCommand toBookDetailCommand(BookDetailRequest request) {
        if (request == null) {
            return null;
        }

        return new BookDetailCommand(
                request.id(),
                request.pageCount(),
                request.publicationYear(),
                request.language(),
                request.coverType(),
                request.dimensions(),
                request.weight(),
                request.translator(),
                request.edition()
        );
    }

    private BookImageResponse toBookImageResponse(BookImage image) {
        return new BookImageResponse(
                image.getId(),
                image.getBookId(),
                image.getFileAssetId(),
                image.getImageUrl(),
                image.getPrimaryImage(),
                image.getSortOrder(),
                image.getAltText(),
                image.getCreatedAt()
        );
    }

    private BookDetailResponse toBookDetailResponse(BookDetail detail) {
        if (detail == null) {
            return null;
        }

        return new BookDetailResponse(
                detail.getId(),
                detail.getBookId(),
                detail.getPageCount(),
                detail.getPublicationYear(),
                detail.getLanguage(),
                detail.getCoverType(),
                detail.getDimensions(),
                detail.getWeight(),
                detail.getTranslator(),
                detail.getEdition()
        );
    }
}
