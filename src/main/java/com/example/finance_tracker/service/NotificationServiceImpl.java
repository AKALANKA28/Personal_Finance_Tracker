package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.repository.NotificationRepository;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service( "notificationService")
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void sendNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    @Override
    public void sendEmailNotification(Notification notification) {
        if (notification.getEmail() != null) {
            // Logic to send email
            System.out.println("Sending email to " + notification.getEmail() + ": " + notification.getMessage());
        }
    }

    @Override
    public List<Notification> getNotificationsByUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public void markNotificationAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public boolean isOwner(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return notification.getUserId().equals(userId);
    }
}