package com.example.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repo;
    private final NotificationProducer producer;
    private final NotificationRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationMapper mapper;
    private final Executor executor = Executors.newFixedThreadPool(10);

    @Override
    public CompletableFuture<NotificationDTO> sendNotification(NotificationDTO dto) {
        return CompletableFuture.supplyAsync(() -> {
            Notification notification = mapper.toEntity(dto);
            notification.setTimestamp(LocalDateTime.now());
            notification.setRead(false);
            Notification saved = repository.save(notification);
            kafkaTemplate.send("user-notification-topic", saved);
            return mapper.toDTO(saved);
        }, executor);
    }

    @Override
    public CompletableFuture<List<NotificationDTO>> getNotifications(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Notification> list = repository.findByUserId(userId);
            return mapper.toDTOList(list);
        }, executor);
    }

    @KafkaListener(topics = {"transaction-topic", "account-topic"}, groupId = "notification-group")
    public void listen(Object event) {
        log.info("Received event: {}", event);
        try {
            Long userId = extractUserId(event);
            String message = createMessage(event);
            sendNotification(NotificationDTO.builder()
                    .userId(userId)
                    .message(message)
                    .build());
        } catch (Exception e) {
            log.error("Failed to process Kafka event: {}", e.getMessage());
        }
    }

    private Long extractUserId(Object event) {
        if (event instanceof LinkedHashMap<?, ?> map && map.containsKey("userId")) {
            return Long.parseLong(map.get("userId").toString());
        }
        return null;
    }

    private String createMessage(Object event) {
        if (event instanceof LinkedHashMap<?, ?> map && map.containsKey("type")) {
            return "New transaction: " + map.get("type") + " of amount " + map.get("amount");
        }
        return "Notification triggered.";
    }
    @Override
    public void processNotification(String rawMessage) {
        // Deserialize JSON to NotificationDTO (or TransactionDTO)
        NotificationDTO dto = new NotificationDTO();
        // Populate from rawMessage

        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setMessage(dto.getMessage());
        notification.setStatus("SENT");
        notification.setTimestamp(LocalDateTime.now());
        repo.save(notification);

        // Forward to user via Kafka (optional)
        producer.sendToUser("user-notification-topic", dto);
    }
}
