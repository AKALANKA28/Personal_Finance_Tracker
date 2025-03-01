package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> sendNotification(@RequestBody Notification notification) {
        notificationService.sendNotification(notification);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable String userId) {
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/mark-as-read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }
}