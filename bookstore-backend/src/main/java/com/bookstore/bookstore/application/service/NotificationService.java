package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.NotificationAssembler;
import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.DeleteNotificationCommand;
import com.bookstore.bookstore.application.command.MarkNotificationReadCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.out.INotificationRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.NotificationResult;
import com.bookstore.bookstore.domain.model.Notification;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final INotificationRepository notificationRepository;
    private final IUserRepository userRepository;
    private final NotificationAssembler notificationAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResult> getMyNotifications(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        return notificationRepository.findAllByUserIdActive(userId).stream()
                .map(notificationAssembler::toResult)
                .toList();
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
        return notificationRepository.findAllActive().stream()
                .map(notificationAssembler::toResult)
                .toList();
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
                false,
                now,
                now,
                null,
                null
        );

        return notificationAssembler.toResult(notificationRepository.save(notification));
    }

    private void requireExistingUser(UUID userId) {
        User user = userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND);
        }
    }
}
