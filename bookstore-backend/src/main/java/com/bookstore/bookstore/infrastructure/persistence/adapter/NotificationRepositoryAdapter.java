package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.INotificationRepository;
import com.bookstore.bookstore.domain.model.Notification;
import com.bookstore.bookstore.infrastructure.persistence.entity.NotificationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.NotificationPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.NotificationJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements INotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final NotificationPersistenceMapper notificationPersistenceMapper;

    @Override
    public List<Notification> findAllActive() {
        return notificationJpaRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .map(notificationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findAllByUserIdActive(UUID userId) {
        return notificationJpaRepository.findAllByDeletedAtIsNullAndUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notificationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Notification> findByIdActive(UUID notificationId) {
        return notificationJpaRepository.findByDeletedAtIsNullAndId(notificationId)
                .map(notificationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Notification> findByIdAndUserIdActive(UUID notificationId, UUID userId) {
        return notificationJpaRepository.findByDeletedAtIsNullAndIdAndUserId(notificationId, userId)
                .map(notificationPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Notification> findByIdIncludingDeleted(UUID notificationId) {
        return notificationJpaRepository.findById(notificationId)
                .map(notificationPersistenceMapper::toDomain);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = notificationJpaRepository.findById(notification.getId())
                .orElseGet(NotificationJpaEntity::new);

        UserJpaEntity user = userJpaRepository.getReferenceById(notification.getUserId());
        notificationPersistenceMapper.copyToEntity(entity, notification, user);
        return notificationPersistenceMapper.toDomain(notificationJpaRepository.save(entity));
    }
}
