package com.example.notification;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {
    CompletableFuture<NotificationDTO> sendNotification(NotificationDTO dto);
    CompletableFuture<List<NotificationDTO>> getNotifications(Long userId);
    void processNotification(String rawMessage);
}