package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.NotificationAssembler;
import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.MarkNotificationReadCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.INotificationRepository;
import com.bookstore.bookstore.application.port.out.INotificationRealtimePublisher;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.NotificationResult;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Notification;
import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private INotificationRepository notificationRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private INotificationRealtimePublisher notificationRealtimePublisher;

    @Mock
    private NotificationAssembler notificationAssembler;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void create_savesNormalizedNotification() {
        UUID userId = UUID.randomUUID();
        NotificationResult expected = new NotificationResult(
                UUID.randomUUID(),
                userId,
                "Title",
                "Content",
                false,
                Instant.EPOCH,
                Instant.EPOCH,
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findByIdIncludingDeleted(userId)).thenReturn(Optional.of(user(userId)));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationAssembler.toResult(any(Notification.class))).thenReturn(expected);

        NotificationResult result = notificationService.create(new CreateNotificationCommand(
                userId,
                " Title ",
                " Content ",
                null,
                null,
                null,
                null
        ));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("Title", captor.getValue().getTitle());
        assertEquals("Content", captor.getValue().getContent());
        assertEquals(false, captor.getValue().isRead());
        assertEquals(expected, result);
    }

    @Test
    void create_whenUserNotFound_rejects() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdIncludingDeleted(userId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> notificationService.create(new CreateNotificationCommand(
                        userId,
                        "Title",
                        "Content",
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void markRead_marksNotificationRead() {
        UUID userId = UUID.randomUUID();
        Notification notification = notification(userId);

        when(notificationRepository.findByIdAndUserIdActive(notification.getId(), userId))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationAssembler.toResult(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            assertEquals(true, saved.isRead());
            assertNotNull(saved.getReadAt());
            return new NotificationResult(
                    saved.getId(),
                    saved.getUserId(),
                    saved.getTitle(),
                    saved.getContent(),
                    saved.isRead(),
                    saved.getCreatedAt(),
                    saved.getUpdatedAt(),
                    saved.getReadAt(),
                    saved.getType(),
                    saved.getTargetType(),
                    saved.getTargetId(),
                    saved.getLink()
            );
        });

        NotificationResult result = notificationService.markRead(
                new MarkNotificationReadCommand(notification.getId(), userId)
        );

        assertEquals(true, result.read());
        assertNotNull(result.readAt());
    }

    private static Notification notification(UUID userId) {
        Instant now = Instant.EPOCH;
        return new Notification(
                UUID.randomUUID(),
                userId,
                "Title",
                "Content",
                false,
                now,
                now,
                null,
                null
        );
    }

    private static User user(UUID userId) {
        Instant now = Instant.EPOCH;
        return new User(
                userId,
                "username",
                "password-hash",
                "0123456789",
                "test@gmail.com",
                UserStatus.ACTIVE,
                false,
                Set.of(),
                now,
                now,
                null
        );
    }
}
