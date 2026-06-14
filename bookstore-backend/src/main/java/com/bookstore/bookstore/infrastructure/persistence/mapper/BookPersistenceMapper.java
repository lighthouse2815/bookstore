package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookDetail;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookDetailJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookImageJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BookPersistenceMapper {

    public Book toDomain(BookJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Book(
                entity.getId(),
                entity.getTitle(),
                entity.getIsbn(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStockQuantity(),
                toDomainImages(entity),
                toDomainDetail(entity.getDetail()),
                entity.getCategoryId(),
                entity.getAuthorId(),
                entity.getPublisherId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public BookJpaEntity toEntity(Book book) {
        BookJpaEntity entity = new BookJpaEntity();
        copyToEntity(entity, book);
        return entity;
    }

    public void copyToEntity(BookJpaEntity entity, Book book) {
        entity.setId(book.getId());
        entity.setTitle(book.getTitle());
        entity.setIsbn(book.getIsbn());
        entity.setDescription(book.getDescription());
        entity.setPrice(book.getPrice());
        entity.setStockQuantity(book.getStockQuantity());
        entity.setImageUrl(book.getPrimaryImageUrl());
        entity.setCategoryId(book.getCategoryId());
        entity.setAuthorId(book.getAuthorId());
        entity.setPublisherId(book.getPublisherId());
        entity.setCreatedAt(book.getCreatedAt());
        entity.setUpdatedAt(book.getUpdatedAt());
        entity.setDeletedAt(book.getDeletedAt());

        Map<UUID, BookImageJpaEntity> currentImages = entity.getImages().stream()
                .collect(Collectors.toMap(BookImageJpaEntity::getId, Function.identity()));

        List<BookImageJpaEntity> mappedImages = book.getImages().stream()
                .map(image -> {
                    BookImageJpaEntity imageEntity = currentImages.getOrDefault(
                            image.getId(),
                            new BookImageJpaEntity()
                    );
                    copyImageToEntity(image, imageEntity, entity);
                    return imageEntity;
                })
                .toList();

        entity.getImages().clear();
        entity.getImages().addAll(mappedImages);

        if (book.getDetail() == null) {
            entity.setDetail(null);
            return;
        }

        BookDetailJpaEntity detailEntity = entity.getDetail();
        if (detailEntity == null || detailEntity.getId() == null || !detailEntity.getId().equals(book.getDetail().getId())) {
            detailEntity = new BookDetailJpaEntity();
        }
        copyDetailToEntity(book.getDetail(), detailEntity, entity);
        entity.setDetail(detailEntity);
    }

    private List<BookImage> toDomainImages(BookJpaEntity entity) {
        List<BookImage> images = entity.getImages().stream()
                .map(this::toDomain)
                .toList();
        if (!images.isEmpty()) {
            return images;
        }

        if (entity.getImageUrl() == null || entity.getImageUrl().isBlank()) {
            return List.of();
        }

        return List.of(new BookImage(
                entity.getId(),
                entity.getId(),
                entity.getImageUrl(),
                true,
                0,
                null,
                entity.getCreatedAt()
        ));
    }

    private BookImage toDomain(BookImageJpaEntity entity) {
        return new BookImage(
                entity.getId(),
                entity.getBook().getId(),
                entity.getImageUrl(),
                entity.getPrimaryImage(),
                entity.getSortOrder(),
                entity.getAltText(),
                entity.getCreatedAt()
        );
    }

    private BookDetail toDomainDetail(BookDetailJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new BookDetail(
                entity.getId(),
                entity.getBook().getId(),
                entity.getPageCount(),
                entity.getPublicationYear(),
                entity.getLanguage(),
                entity.getCoverType(),
                entity.getDimensions(),
                entity.getWeight(),
                entity.getTranslator(),
                entity.getEdition()
        );
    }

    private void copyImageToEntity(BookImage image, BookImageJpaEntity entity, BookJpaEntity bookEntity) {
        entity.setId(image.getId());
        entity.setBook(bookEntity);
        entity.setImageUrl(image.getImageUrl());
        entity.setPrimaryImage(image.getPrimaryImage());
        entity.setSortOrder(image.getSortOrder());
        entity.setAltText(image.getAltText());
        entity.setCreatedAt(image.getCreatedAt());
    }

    private void copyDetailToEntity(BookDetail detail, BookDetailJpaEntity entity, BookJpaEntity bookEntity) {
        entity.setId(detail.getId());
        entity.setBook(bookEntity);
        entity.setPageCount(detail.getPageCount());
        entity.setPublicationYear(detail.getPublicationYear());
        entity.setLanguage(detail.getLanguage());
        entity.setCoverType(detail.getCoverType());
        entity.setDimensions(detail.getDimensions());
        entity.setWeight(detail.getWeight());
        entity.setTranslator(detail.getTranslator());
        entity.setEdition(detail.getEdition());
    }
}
