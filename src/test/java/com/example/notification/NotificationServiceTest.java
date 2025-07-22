package com.example.notification;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.times;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    public void testProcessNotification_successful() {
        // Given
        String message = "{\"userId\":1,\"message\":\"Transaction completed\"}";

        // When
        notificationService.processNotification(message);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(notificationCaptor.capture());
        Notification saved = notificationCaptor.getValue();

        assertEquals(Optional.of(1L), saved.getUserId());
        assertEquals("Transaction completed", saved.getMessage());
        assertEquals("SENT", saved.getStatus());
    }

    @Test
    public void testProcessNotification_invalidJson() {
        // Given
        String message = "invalid-json";

        // When / Then
        assertThrows(RuntimeException.class, () -> {
            notificationService.processNotification(message);
        });
    }

    @Test
    public void testSendToUser() {
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(1L);
        dto.setMessage("Test Message");

        notificationProducer.sendToUser("user-notification-topic", dto);

        verify(notificationProducer, times(1)).sendToUser(eq("user-notification-topic"), eq(dto));
    }
}
