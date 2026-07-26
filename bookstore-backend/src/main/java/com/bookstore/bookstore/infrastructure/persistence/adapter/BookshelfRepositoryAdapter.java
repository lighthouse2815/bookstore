package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IBookshelfRepository;
import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.domain.model.BookshelfItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.BookshelfItemPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.mapper.BookshelfPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookshelfItemJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookshelfJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookshelfRepositoryAdapter implements IBookshelfRepository {

    private final BookshelfJpaRepository bookshelfJpaRepository;
    private final BookshelfItemJpaRepository bookshelfItemJpaRepository;
    private final BookshelfPersistenceMapper bookshelfPersistenceMapper;
    private final BookshelfItemPersistenceMapper bookshelfItemPersistenceMapper;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;

    @Override
    public List<Bookshelf> findAllByUserIdActive(UUID userId) {
        return bookshelfJpaRepository.findAllByUserIdActive(userId).stream()
                .map(bookshelfPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Bookshelf> findByIdAndUserIdActive(UUID shelfId, UUID userId) {
        return bookshelfJpaRepository.findByIdAndUserIdActive(shelfId, userId)
                .map(bookshelfPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Bookshelf> findByUserIdAndName(UUID userId, String name) {
        return bookshelfJpaRepository.findByUserIdAndName(userId, name)
                .map(bookshelfPersistenceMapper::toDomain);
    }

    @Override
    public long countActiveItemsByShelfId(UUID shelfId) {
        return bookshelfItemJpaRepository.countActiveByShelfId(shelfId);
    }

    @Override
    public Map<UUID, Long> countActiveItemsByShelfIds(Collection<UUID> shelfIds) {
        if (shelfIds == null || shelfIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Long> counts = new LinkedHashMap<>();
        for (Object[] row : bookshelfItemJpaRepository.countActiveByShelfIds(shelfIds)) {
            counts.put((UUID) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Override
    public List<BookshelfItem> findAllItemsByShelfIdActive(UUID shelfId) {
        return bookshelfItemJpaRepository.findAllByShelfIdActive(shelfId).stream()
                .map(bookshelfItemPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<BookshelfItem> findAllItemsByShelfIdsActive(Collection<UUID> shelfIds) {
        if (shelfIds == null || shelfIds.isEmpty()) {
            return List.of();
        }

        return bookshelfItemJpaRepository.findAllByShelfIdsActive(shelfIds).stream()
                .map(bookshelfItemPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<BookshelfItem> findItemByShelfIdAndBookId(UUID shelfId, UUID bookId) {
        return bookshelfItemJpaRepository.findByShelfIdAndBookId(shelfId, bookId)
                .map(bookshelfItemPersistenceMapper::toDomain);
    }

    @Override
    public Bookshelf save(Bookshelf bookshelf) {
        BookshelfJpaEntity entity = bookshelfJpaRepository.findById(bookshelf.getId())
                .orElseGet(BookshelfJpaEntity::new);
        UserJpaEntity user = userJpaRepository.getReferenceById(bookshelf.getUserId());
        bookshelfPersistenceMapper.copyToEntity(bookshelf, entity, user);
        return bookshelfPersistenceMapper.toDomain(bookshelfJpaRepository.save(entity));
    }

    @Override
    public BookshelfItem saveItem(BookshelfItem bookshelfItem) {
        BookshelfItemJpaEntity entity = bookshelfItemJpaRepository.findById(bookshelfItem.getId())
                .orElseGet(BookshelfItemJpaEntity::new);
        BookshelfJpaEntity shelf = bookshelfJpaRepository.getReferenceById(bookshelfItem.getShelfId());
        BookJpaEntity book = bookJpaRepository.getReferenceById(bookshelfItem.getBookId());
        bookshelfItemPersistenceMapper.copyToEntity(bookshelfItem, entity, shelf, book);
        return bookshelfItemPersistenceMapper.toDomain(bookshelfItemJpaRepository.save(entity));
    }

    @Override
    public List<BookshelfItem> saveAllItems(List<BookshelfItem> bookshelfItems) {
        if (bookshelfItems == null || bookshelfItems.isEmpty()) {
            return List.of();
        }

        Map<UUID, BookshelfItemJpaEntity> existingEntities = bookshelfItemJpaRepository.findAllById(
                        bookshelfItems.stream().map(BookshelfItem::getId).toList()
                ).stream()
                .collect(java.util.stream.Collectors.toMap(
                        BookshelfItemJpaEntity::getId,
                        java.util.function.Function.identity()
                ));
        Map<UUID, BookshelfJpaEntity> shelvesById = resolveShelves(bookshelfItems);
        Map<UUID, BookJpaEntity> booksById = resolveBooks(bookshelfItems);

        List<BookshelfItemJpaEntity> entities = bookshelfItems.stream()
                .map(item -> {
                    BookshelfItemJpaEntity entity = existingEntities.getOrDefault(item.getId(), new BookshelfItemJpaEntity());
                    bookshelfItemPersistenceMapper.copyToEntity(
                            item,
                            entity,
                            shelvesById.get(item.getShelfId()),
                            booksById.get(item.getBookId())
                    );
                    return entity;
                })
                .toList();

        return bookshelfItemJpaRepository.saveAll(entities).stream()
                .map(bookshelfItemPersistenceMapper::toDomain)
                .toList();
    }

    private Map<UUID, BookshelfJpaEntity> resolveShelves(List<BookshelfItem> bookshelfItems) {
        return bookshelfItems.stream()
                .map(BookshelfItem::getShelfId)
                .distinct()
                .collect(java.util.stream.Collectors.toMap(
                        java.util.function.Function.identity(),
                        bookshelfJpaRepository::getReferenceById
                ));
    }

    private Map<UUID, BookJpaEntity> resolveBooks(List<BookshelfItem> bookshelfItems) {
        return bookshelfItems.stream()
                .map(BookshelfItem::getBookId)
                .distinct()
                .collect(java.util.stream.Collectors.toMap(
                        java.util.function.Function.identity(),
                        bookJpaRepository::getReferenceById
                ));
    }
}
