package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')") // Only authenticated users can send notifications
    public ResponseEntity<Void> sendNotification(@RequestBody Notification notification, @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        notification.setUserId(authenticatedUserId); // Set the authenticated user's ID
        notificationService.sendNotification(notification);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')") // Users can view their own notifications; admins can view all
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/mark-as-read")
    @PreAuthorize("@notificationService.isOwner(#id, authentication.principal.id) or hasRole('ROLE_ADMIN')") // Users can mark their own notifications as read; admins can mark any
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }


}