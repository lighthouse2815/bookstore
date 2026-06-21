package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.NotificationResult;

public interface INotificationRealtimePublisher {

    void publishToUser(String userId, NotificationResult notification);
}
