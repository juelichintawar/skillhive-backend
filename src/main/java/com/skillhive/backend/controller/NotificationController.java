package com.skillhive.backend.controller;

import com.skillhive.backend.model.Notification;
import com.skillhive.backend.model.User;
import com.skillhive.backend.repository.NotificationRepository;
import com.skillhive.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(user.getId())
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                notificationRepository
                        .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId())
        );
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                notificationRepository
                        .countByUserIdAndIsReadFalse(user.getId())
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot update this notification");
        }

        notification.setRead(true);

        return ResponseEntity.ok(
                notificationRepository.save(notification)
        );
    }
}