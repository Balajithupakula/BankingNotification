package com.example.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository repo;
    private final NotificationService notificationService;

    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<NotificationDTO>> send(@RequestBody NotificationDTO dto) {
        return notificationService.sendNotification(dto)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<List<NotificationDTO>>> getByUser(@PathVariable Long userId) {
        return notificationService.getNotifications(userId)
                .thenApply(ResponseEntity::ok);
    }
    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId) {
        return repo.findAll().stream()
                .filter(n -> n.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}

