package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.NotificationAssembler;
import com.bookstore.bookstore.application.command.BroadcastNotificationCommand;
import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.DeleteNotificationCommand;
import com.bookstore.bookstore.application.command.MarkNotificationReadCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.out.INotificationRepository;
import com.bookstore.bookstore.application.port.out.INotificationRealtimePublisher;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.NotificationBroadcastResult;
import com.bookstore.bookstore.application.result.NotificationResult;
import com.bookstore.bookstore.application.result.NotificationSliceResult;
import com.bookstore.bookstore.domain.model.Notification;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final INotificationRepository notificationRepository;
    private final IUserRepository userRepository;
    private final INotificationRealtimePublisher notificationRealtimePublisher;
    private final NotificationAssembler notificationAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResult> getMyNotifications(UUID userId) {
        return getMyNotifications(userId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResult> getMyNotifications(UUID userId, Boolean read) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        return toResults(notificationRepository.findAllByUserIdActive(userId, read));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSliceResult getMyNotifications(UUID userId, PageQuery pageQuery, Boolean read) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        return new NotificationSliceResult(
                toResults(notificationRepository.findPageByUserIdActive(
                        userId,
                        read,
                        pageQuery.page(),
                        pageQuery.size()
                )),
                notificationRepository.countByUserIdActive(userId, read),
                pageQuery.page(),
                pageQuery.size()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnreadNotifications(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        return notificationRepository.countUnreadByUserIdActive(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResult getMyNotification(UUID userId, UUID notificationId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        if (notificationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "notificationId");
        }

        return notificationRepository.findByIdAndUserIdActive(notificationId, userId)
                .map(notificationAssembler::toResult)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationResult markRead(MarkNotificationReadCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Notification notification = notificationRepository.findByIdAndUserIdActive(
                        command.notificationId(),
                        command.userId()
                )
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markRead();
        return notificationAssembler.toResult(notificationRepository.save(notification));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        List<Notification> unreadNotifications = notificationRepository.findAllUnreadByUserIdActive(userId);
        if (unreadNotifications.isEmpty()) {
            return;
        }

        unreadNotifications.forEach(Notification::markRead);
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteNotificationCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Notification notification = notificationRepository.findByIdAndUserIdActive(
                        command.notificationId(),
                        command.userId()
                )
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOTIFICATION_NOT_FOUND));

        notification.softDelete();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResult> getAll() {
        return toResults(notificationRepository.findAllActive());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSliceResult getAll(PageQuery pageQuery) {
        return new NotificationSliceResult(
                toResults(notificationRepository.findPageActive(pageQuery.page(), pageQuery.size())),
                notificationRepository.countActive(),
                pageQuery.page(),
                pageQuery.size()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResult getById(UUID notificationId) {
        if (notificationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "notificationId");
        }

        return notificationRepository.findByIdActive(notificationId)
                .map(notificationAssembler::toResult)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationResult create(CreateNotificationCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        requireExistingUser(command.userId());

        Instant now = Instant.now();
        Notification notification = new Notification(
                UUID.randomUUID(),
                command.userId(),
                StringUtils.trimToNull(command.title()),
                StringUtils.trimToNull(command.content()),
                StringUtils.trimToNull(command.type()),
                StringUtils.trimToNull(command.targetType()),
                command.targetId(),
                StringUtils.trimToNull(command.link()),
                false,
                now,
                now,
                null,
                null
        );

        NotificationResult result = notificationAssembler.toResult(notificationRepository.save(notification));
        publishAfterCommit(List.of(result));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationBroadcastResult broadcast(BroadcastNotificationCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Instant now = Instant.now();
        List<Notification> notifications = userRepository.findAllActive().stream()
                .map(user -> new Notification(
                        UUID.randomUUID(),
                        user.getId(),
                        StringUtils.trimToNull(command.title()),
                        StringUtils.trimToNull(command.content()),
                        StringUtils.trimToNull(command.type()),
                        StringUtils.trimToNull(command.targetType()),
                        command.targetId(),
                        StringUtils.trimToNull(command.link()),
                        false,
                        now,
                        now,
                        null,
                        null
                ))
                .toList();

        if (notifications.isEmpty()) {
            return new NotificationBroadcastResult(0);
        }

        List<NotificationResult> results = toResults(notificationRepository.saveAll(notifications));
        publishAfterCommit(results);
        return new NotificationBroadcastResult(results.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(UUID notificationId) {
        if (notificationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "notificationId");
        }

        Notification notification = notificationRepository.findByIdActive(notificationId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOTIFICATION_NOT_FOUND));

        notification.softDelete();
        notificationRepository.save(notification);
    }

    private void requireExistingUser(UUID userId) {
        User user = userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND);
        }
    }

    private List<NotificationResult> toResults(List<Notification> notifications) {
        return notifications.stream()
                .map(notificationAssembler::toResult)
                .toList();
    }

    private void publishAfterCommit(List<NotificationResult> notifications) {
        if (notifications.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notifications.forEach(NotificationService.this::publishQuietly);
                }
            });
            return;
        }

        notifications.forEach(this::publishQuietly);
    }

    private void publishQuietly(NotificationResult notification) {
        try {
            notificationRealtimePublisher.publishToUser(notification.userId().toString(), notification);
        } catch (RuntimeException exception) {
            log.warn("Failed to publish notification {} to user {}", notification.notificationId(), notification.userId(), exception);
        }
    }
}
