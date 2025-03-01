package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Notification notification);
    List<Notification> getNotificationsByUser(String userId);
    void markNotificationAsRead(String notificationId);
}