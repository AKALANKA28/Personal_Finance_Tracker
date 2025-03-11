package com.example.finance_tracker.unit;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.repository.NotificationRepository;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotification_Success() {
        // Arrange
        Notification notification = new Notification();
        notification.setUserId("123");
        notification.setMessage("Test notification");

        when(notificationRepository.save(notification)).thenReturn(notification);

        // Act
        notificationService.sendNotification(notification);

        // Assert
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void sendEmailNotification_Success() {
        // Arrange
        Notification notification = new Notification();
        notification.setEmail("test@example.com");
        notification.setMessage("Test email notification");

        // Act
        notificationService.sendEmailNotification(notification);

        // Assert
        // Verify that the email logic is executed (e.g., System.out.println)
    }

    @Test
    void getNotificationsByUser_Success() {
        // Arrange
        Notification notification = new Notification();
        notification.setUserId("123");

        when(notificationRepository.findByUserId("123")).thenReturn(Collections.singletonList(notification));

        // Act
        List<Notification> notifications = notificationService.getNotificationsByUser("123");

        // Assert
        assertEquals(1, notifications.size());
        verify(notificationRepository, times(1)).findByUserId("123");
    }

    @Test
    void markNotificationAsRead_Success() {
        // Arrange
        Notification notification = new Notification();
        notification.setId("123");
        notification.setRead(false);

        when(notificationRepository.findById("123")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        // Act
        notificationService.markNotificationAsRead("123");

        // Assert
        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void markNotificationAsRead_NotificationNotFound() {
        // Arrange
        when(notificationRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.markNotificationAsRead("123");
        });

        assertEquals("Notification not found", exception.getMessage());
    }

    @Test
    void isOwner_Success() {
        // Arrange
        Notification notification = new Notification();
        notification.setId("123");
        notification.setUserId("456");

        when(notificationRepository.findById("123")).thenReturn(Optional.of(notification));

        // Act
        boolean result = notificationService.isOwner("123", "456");

        // Assert
        assertTrue(result);
    }

    @Test
    void isOwner_NotificationNotFound() {
        // Arrange
        when(notificationRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.isOwner("123", "456");
        });

        assertEquals("Notification not found", exception.getMessage());
    }

    @Test
    void isOwner_NotOwner() {
        // Arrange
        Notification notification = new Notification();
        notification.setId("123");
        notification.setUserId("456");

        when(notificationRepository.findById("123")).thenReturn(Optional.of(notification));

        // Act
        boolean result = notificationService.isOwner("123", "789");

        // Assert
        assertFalse(result);
    }
}