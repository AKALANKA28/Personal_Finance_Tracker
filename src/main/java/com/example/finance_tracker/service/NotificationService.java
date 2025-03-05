package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Notification notification);
    void sendEmailNotification(Notification notification); // New method for email notifications
    List<Notification> getNotificationsByUser(String userId);
    void markNotificationAsRead(String notificationId);

    boolean isOwner(String notificationId, String userId);

}