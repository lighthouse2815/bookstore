package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.INotificationRepository;
import com.bookstore.bookstore.domain.model.Notification;
import com.bookstore.bookstore.infrastructure.persistence.entity.NotificationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.NotificationPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.NotificationJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements INotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationPersistenceMapper notificationPersistenceMapper;

    @Override
    public List<Notification> findAllActive() {
        return notificationJpaRepository.findAllActive().stream()
                .map(notificationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findAllByUserIdActive(UUID userId) {
        return notificationJpaRepository.findAllByUserIdActive(userId).stream()
                .map(notificationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Notification> findByIdActive(UUID notificationId) {
        return notificationJpaRepository.findByIdActive(notificationId)
                .map(notificationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Notification> findByIdAndUserIdActive(UUID notificationId, UUID userId) {
        return notificationJpaRepository.findByIdAndUserIdActive(notificationId, userId)
                .map(notificationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Notification> findByIdIncludingDeleted(UUID notificationId) {
        return notificationJpaRepository.findByIdIncludingDeleted(notificationId)
                .map(notificationPersistenceMapper::toDomain);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = notificationJpaRepository.findByIdIncludingDeleted(notification.getId())
                .orElseGet(NotificationJpaEntity::new);
        notificationPersistenceMapper.copyToEntity(entity, notification);
        return notificationPersistenceMapper.toDomain(notificationJpaRepository.save(entity));
    }
}
