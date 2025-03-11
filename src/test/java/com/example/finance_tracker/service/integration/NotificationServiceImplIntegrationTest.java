package com.example.finance_tracker.service.integration;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.repository.NotificationRepository;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Reset the database after each test
public class NotificationServiceImplIntegrationTest {

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Notification notification;

    @BeforeEach
    void setUp() {
        // Clear the notifications collection before each test
        mongoTemplate.dropCollection(Notification.class);

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
        // Using MongoTemplate to verify
        Query query = new Query(Criteria.where("id").is(notification.getId()));
        Notification savedNotification = mongoTemplate.findOne(query, Notification.class);

        assertNotNull(savedNotification);
        assertEquals("user123", savedNotification.getUserId());
        assertEquals("Test notification", savedNotification.getMessage());
        assertFalse(savedNotification.isRead());
    }

    @Test
    void getNotificationsByUser_Success() {
        // Arrange
        mongoTemplate.save(notification);

        // Act
        List<Notification> result = notificationService.getNotificationsByUser("user123");

        // Assert
        assertEquals(1, result.size());
        assertEquals("user123", result.get(0).getUserId());
        assertEquals("Test notification", result.get(0).getMessage());
        assertFalse(result.get(0).isRead());

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("userId").is("user123"));
        List<Notification> mongoResult = mongoTemplate.find(query, Notification.class);
        assertEquals(1, mongoResult.size());
    }

    @Test
    void markNotificationAsRead_Success() {
        // Arrange
        Notification savedNotification = mongoTemplate.save(notification);

        // Act
        notificationService.markNotificationAsRead(savedNotification.getId());

        // Assert
        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("id").is(savedNotification.getId()));
        Notification updatedNotification = mongoTemplate.findOne(query, Notification.class);

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

        // Verify with MongoTemplate that notification doesn't exist
        Query query = new Query(Criteria.where("id").is("nonExistentId"));
        Notification nonExistentNotification = mongoTemplate.findOne(query, Notification.class);
        assertNull(nonExistentNotification);
    }

    @Test
    void isOwner_Success() {
        // Arrange
        Notification savedNotification = mongoTemplate.save(notification);

        // Act
        boolean result = notificationService.isOwner(savedNotification.getId(), "user123");

        // Assert
        assertTrue(result);

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("id").is(savedNotification.getId())
                .and("userId").is("user123"));
        Notification foundNotification = mongoTemplate.findOne(query, Notification.class);
        assertNotNull(foundNotification);
    }

    @Test
    void isOwner_NotFound_ThrowsException() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.isOwner("nonExistentId", "user123");
        });

        assertEquals("Notification not found", exception.getMessage());

        // Verify with MongoTemplate that notification doesn't exist
        Query query = new Query(Criteria.where("id").is("nonExistentId"));
        Notification nonExistentNotification = mongoTemplate.findOne(query, Notification.class);
        assertNull(nonExistentNotification);
    }

    @Test
    void getNotificationsByUser_NoNotifications_ReturnsEmptyList() {
        // Act
        List<Notification> result = notificationService.getNotificationsByUser("nonExistentUser");

        // Assert
        assertTrue(result.isEmpty());

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("userId").is("nonExistentUser"));
        List<Notification> mongoResult = mongoTemplate.find(query, Notification.class);
        assertTrue(mongoResult.isEmpty());
    }

    @Test
    void isOwner_WrongUser_ReturnsFalse() {
        // Arrange
        Notification savedNotification = mongoTemplate.save(notification);

        // Act
        boolean result = notificationService.isOwner(savedNotification.getId(), "wrongUser");

        // Assert
        assertFalse(result);

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("id").is(savedNotification.getId())
                .and("userId").is("wrongUser"));
        Notification foundNotification = mongoTemplate.findOne(query, Notification.class);
        assertNull(foundNotification);
    }
}