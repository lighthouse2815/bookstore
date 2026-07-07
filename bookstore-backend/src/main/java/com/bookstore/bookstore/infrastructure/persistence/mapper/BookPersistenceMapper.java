package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookDetail;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.infrastructure.persistence.entity.AuthorJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookDetailJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookImageJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookPersistenceMapper {

    private final FileAssetPersistenceMapper fileAssetPersistenceMapper;

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
                entity.getCategory() != null ? entity.getCategory().getId() : null,
                entity.getAuthor() != null ? entity.getAuthor().getId() : null,
                entity.getPublisher() != null ? entity.getPublisher().getId() : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            BookJpaEntity entity,
            Book book,
            CategoryJpaEntity category,
            AuthorJpaEntity author,
            PublisherJpaEntity publisher,
            Map<UUID, FileAssetJpaEntity> fileAssetsById
    ) {
        entity.setId(book.getId());
        entity.setTitle(book.getTitle());
        entity.setIsbn(book.getIsbn());
        entity.setDescription(book.getDescription());
        entity.setPrice(book.getPrice());
        entity.setStockQuantity(book.getStockQuantity());
        entity.setImageUrl(null);

        entity.setCategory(category);
        entity.setAuthor(author);
        entity.setPublisher(publisher);

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
                    copyImageToEntity(
                            image,
                            imageEntity,
                            entity,
                            fileAssetsById.get(image.getFileAssetId())
                    );
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
        return entity.getImages().stream()
                .filter(image -> image.getFileAsset() != null)
                .map(this::toDomain)
                .toList();
    }

    private BookImage toDomain(BookImageJpaEntity entity) {
        return new BookImage(
                entity.getId(),
                entity.getBook().getId(),
                fileAssetPersistenceMapper.toDomain(entity.getFileAsset()),
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

    private void copyImageToEntity(
            BookImage image,
            BookImageJpaEntity entity,
            BookJpaEntity bookEntity,
            FileAssetJpaEntity fileAsset
    ) {
        entity.setId(image.getId());
        entity.setBook(bookEntity);
        entity.setFileAsset(fileAsset);
        entity.setImageUrl(null);
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
