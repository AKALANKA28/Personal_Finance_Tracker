package com.example.finance_tracker.service.intergration;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.repository.NotificationRepository;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class NotificationServiceImplIntegrationTest {

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId("123");
        notification.setUserId("user123");
        notification.setMessage("Test notification");
        notification.setRead(false);
    }

    @Test
    void sendNotification_Success() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.sendNotification(notification);

        // Assert
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void getNotificationsByUser_Success() {
        // Arrange
        when(notificationRepository.findByUserId("user123")).thenReturn(Collections.singletonList(notification));

        // Act
        List<Notification> result = notificationService.getNotificationsByUser("user123");

        // Assert
        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getId());
        assertEquals("user123", result.get(0).getUserId());
        assertEquals("Test notification", result.get(0).getMessage());
        assertFalse(result.get(0).isRead());

        // Verify
        verify(notificationRepository, times(1)).findByUserId("user123");
    }

    @Test
    void markNotificationAsRead_Success() {
        // Arrange
        when(notificationRepository.findById("123")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.markNotificationAsRead("123");

        // Assert
        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).findById("123");
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void markNotificationAsRead_NotFound_ThrowsException() {
        // Arrange
        when(notificationRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.markNotificationAsRead("123");
        });

        assertEquals("Notification not found", exception.getMessage());

        // Verify
        verify(notificationRepository, times(1)).findById("123");
    }

    @Test
    void isOwner_Success() {
        // Arrange
        when(notificationRepository.findById("123")).thenReturn(Optional.of(notification));

        // Act
        boolean result = notificationService.isOwner("123", "user123");

        // Assert
        assertTrue(result);

        // Verify
        verify(notificationRepository, times(1)).findById("123");
    }

    @Test
    void isOwner_NotFound_ThrowsException() {
        // Arrange
        when(notificationRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.isOwner("123", "user123");
        });

        assertEquals("Notification not found", exception.getMessage());

        // Verify
        verify(notificationRepository, times(1)).findById("123");
    }
}