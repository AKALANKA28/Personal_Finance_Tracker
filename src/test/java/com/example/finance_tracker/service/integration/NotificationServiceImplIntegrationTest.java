package com.example.finance_tracker.service.integration;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.repository.NotificationRepository;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Reset the database after each test
public class NotificationServiceImplIntegrationTest {

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setUserId("user123");
        notification.setMessage("Test notification");
        notification.setRead(false);
    }

    @Test
    void sendNotification_Success() {
        // Act
        notificationService.sendNotification(notification);

        // Assert
        Notification savedNotification = notificationRepository.findById(notification.getId()).orElse(null);
        assertNotNull(savedNotification);
        assertEquals("user123", savedNotification.getUserId());
        assertEquals("Test notification", savedNotification.getMessage());
        assertFalse(savedNotification.isRead());
    }

    @Test
    void getNotificationsByUser_Success() {
        // Arrange
        notificationRepository.save(notification);

        // Act
        List<Notification> result = notificationService.getNotificationsByUser("user123");

        // Assert
        assertEquals("user123", result.get(0).getUserId());
        assertEquals("Test notification", result.get(0).getMessage());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void markNotificationAsRead_Success() {
        // Arrange
        Notification savedNotification = notificationRepository.save(notification);

        // Act
        notificationService.markNotificationAsRead(savedNotification.getId());

        // Assert
        Notification updatedNotification = notificationRepository.findById(savedNotification.getId()).orElse(null);
        assertNotNull(updatedNotification);
        assertTrue(updatedNotification.isRead());
    }

    @Test
    void markNotificationAsRead_NotFound_ThrowsException() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.markNotificationAsRead("nonExistentId");
        });

        assertEquals("Notification not found", exception.getMessage());
    }

    @Test
    void isOwner_Success() {
        // Arrange
        Notification savedNotification = notificationRepository.save(notification);

        // Act
        boolean result = notificationService.isOwner(savedNotification.getId(), "user123");

        // Assert
        assertTrue(result);
    }

    @Test
    void isOwner_NotFound_ThrowsException() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.isOwner("nonExistentId", "user123");
        });

        assertEquals("Notification not found", exception.getMessage());
    }
}