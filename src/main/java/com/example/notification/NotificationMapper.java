package com.example.notification;

import java.util.List;

@Mappe(componentModel = "spring")
public interface NotificationMapper {
    NotificationDTO toDTO(Notification notification);
    Notification toEntity(NotificationDTO dto);
    List<NotificationDTO> toDTOList(List<Notification> notifications);
}
