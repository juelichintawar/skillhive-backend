package com.skillhive.backend.service;

import com.skillhive.backend.model.Notification;
import com.skillhive.backend.model.User;
import com.skillhive.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(
            User user,
            String message,
            String type) {

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }
}