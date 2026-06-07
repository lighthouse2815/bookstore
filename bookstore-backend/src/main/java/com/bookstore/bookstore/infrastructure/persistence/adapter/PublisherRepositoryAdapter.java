package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.domain.model.Publisher;
import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.PublisherPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.PublisherJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PublisherRepositoryAdapter implements IPublisherRepository {

    private final PublisherJpaRepository publisherJpaRepository;
    private final PublisherPersistenceMapper publisherPersistenceMapper;

    @Override
    public List<Publisher> findAllActive() {
        return publisherJpaRepository.findAllActive().stream()
                .map(publisherPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Publisher> findAllIncludingDeleted() {
        return publisherJpaRepository.findAllIncludingDeleted().stream()
                .map(publisherPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Publisher> findByIdActive(UUID publisherId) {
        return publisherJpaRepository.findByIdActive(publisherId)
                .map(publisherPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Publisher> findByIdIncludingDeleted(UUID publisherId) {
        return publisherJpaRepository.findByIdIncludingDeleted(publisherId)
                .map(publisherPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Publisher> findByNameActive(String publisherName) {
        return publisherJpaRepository.findByNameActive(publisherName)
                .map(publisherPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID publisherId) {
        return publisherJpaRepository.existsByIdIncludingDeleted(publisherId);
    }

    @Override
    public boolean existsByNameIncludingDeleted(String publisherName) {
        return publisherJpaRepository.existsByNameIncludingDeleted(publisherName);
    }

    @Override
    public Publisher save(Publisher publisher) {
        PublisherJpaEntity entity = publisherJpaRepository.findByIdIncludingDeleted(publisher.getId())
                .orElseGet(PublisherJpaEntity::new);
        publisherPersistenceMapper.copyToEntity(entity, publisher);
        return publisherPersistenceMapper.toDomain(publisherJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID publisherId) {
        publisherJpaRepository.deleteById(publisherId);
    }
}
