package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.Publisher;
import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.PublisherPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.FileAssetJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.PublisherJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PublisherRepositoryAdapter implements IPublisherRepository {

    private final PublisherJpaRepository publisherJpaRepository;
    private final FileAssetJpaRepository fileAssetJpaRepository;
    private final PublisherPersistenceMapper publisherPersistenceMapper;

    @Override
    public List<Publisher> findAllActive() {
        return publisherJpaRepository.findAllByDeletedAtIsNull().stream()
                .map(publisherPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<Publisher> findPageActive(int page, int size) {
        var resultPage = publisherJpaRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc(PageRequest.of(page, size));
        return new PageSliceResult<>(
                resultPage.stream().map(publisherPersistenceMapper::toDomain).toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public List<Publisher> findAllIncludingDeleted() {
        return publisherJpaRepository.findAll().stream()
                .map(publisherPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Publisher> findByIdActive(UUID publisherId) {
        return publisherJpaRepository.findByIdAndDeletedAtIsNull(publisherId)
                .map(publisherPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Publisher> findByIdIncludingDeleted(UUID publisherId) {
        return publisherJpaRepository.findById(publisherId)
                .map(publisherPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Publisher> findByNameActive(String publisherName) {
        return publisherJpaRepository.findByNameAndDeletedAtIsNull(publisherName)
                .map(publisherPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID publisherId) {
        return publisherJpaRepository.existsById(publisherId);
    }

    @Override
    public boolean existsByNameIncludingDeleted(String publisherName) {
        return publisherJpaRepository.existsByName(publisherName);
    }

    @Override
    public Publisher save(Publisher publisher) {
        PublisherJpaEntity entity = publisherJpaRepository.findById(publisher.getId())
                .orElseGet(PublisherJpaEntity::new);
        var logoFileAsset = publisher.getLogoFileAssetId() == null
                ? null
                : fileAssetJpaRepository.getReferenceById(publisher.getLogoFileAssetId());
        publisherPersistenceMapper.copyToEntity(entity, publisher, logoFileAsset);
        return publisherPersistenceMapper.toDomain(publisherJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID publisherId) {
        publisherJpaRepository.deleteById(publisherId);
    }
}
