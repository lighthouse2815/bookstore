package com.bookstore.bookstore.domain.model;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.validation.Guard;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BookDetail {

    private UUID id;
    private UUID bookId;
    private Integer pageCount;
    private Integer publicationYear;
    private String language;
    private String coverType;
    private String dimensions;
    private Integer weight;
    private String translator;
    private String edition;

    public BookDetail(
            UUID id,
            UUID bookId,
            Integer pageCount,
            Integer publicationYear,
            String language,
            String coverType,
            String dimensions,
            Integer weight,
            String translator,
            String edition
    ) {
        this.id = Guard.notNull(id, DomainErrorCode.INVALID_BOOK_DETAIL_ID, "id");
        this.bookId = Guard.notNull(bookId, DomainErrorCode.INVALID_BOOK_DETAIL_BOOK_ID, "bookId");
        setPageCount(pageCount);
        setPublicationYear(publicationYear);
        setLanguage(language);
        setCoverType(coverType);
        setDimensions(dimensions);
        setWeight(weight);
        setTranslator(translator);
        setEdition(edition);
    }

    private void setPageCount(Integer pageCount) {
        if (pageCount != null && pageCount <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOK_DETAIL_PAGE_COUNT, "pageCount");
        }
        this.pageCount = pageCount;
    }

    private void setPublicationYear(Integer publicationYear) {
        if (publicationYear != null && publicationYear <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOK_DETAIL_PUBLICATION_YEAR, "publicationYear");
        }
        this.publicationYear = publicationYear;
    }

    private void setLanguage(String language) {
        this.language = Guard.notBlankOrNull(language, DomainErrorCode.INVALID_BOOK_DETAIL_LANGUAGE, "language");
    }

    private void setCoverType(String coverType) {
        this.coverType = Guard.notBlankOrNull(
                coverType,
                DomainErrorCode.INVALID_BOOK_DETAIL_COVER_TYPE,
                "coverType"
        );
    }

    private void setDimensions(String dimensions) {
        this.dimensions = Guard.notBlankOrNull(
                dimensions,
                DomainErrorCode.INVALID_BOOK_DETAIL_DIMENSIONS,
                "dimensions"
        );
    }

    private void setWeight(Integer weight) {
        if (weight != null && weight <= 0) {
            throw new DomainException(DomainErrorCode.INVALID_BOOK_DETAIL_WEIGHT, "weight");
        }
        this.weight = weight;
    }

    private void setTranslator(String translator) {
        this.translator = Guard.notBlankOrNull(
                translator,
                DomainErrorCode.INVALID_BOOK_DETAIL_TRANSLATOR,
                "translator"
        );
    }

    private void setEdition(String edition) {
        this.edition = Guard.notBlankOrNull(edition, DomainErrorCode.INVALID_BOOK_DETAIL_EDITION, "edition");
    }
}
