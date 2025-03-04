package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Notification notification);
    void sendNotification(String userId, String title, String message);
    List<Notification> getNotificationsByUser(String userId);
    void markNotificationAsRead(String notificationId);
}