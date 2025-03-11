package com.example.finance_tracker.integration.controller;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.repository.NotificationRepository;
import com.example.finance_tracker.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
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
    @WithMockUser(username = "user123", roles = {"USER"})
    void sendNotification_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user123\",\"message\":\"Test notification\",\"read\":false}"))
                .andExpect(status().isOk());

        // Verify
        verify(notificationService, times(1)).sendNotification(any(Notification.class));
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getNotificationsByUser_Success() throws Exception {
        // Arrange
        when(notificationService.getNotificationsByUser("user123")).thenReturn(Collections.singletonList(notification));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notifications/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].message").value("Test notification"))
                .andExpect(jsonPath("$[0].read").value(false));

        // Verify
        verify(notificationService, times(1)).getNotificationsByUser("user123");
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void markNotificationAsRead_Success() throws Exception {
        // Arrange
        doNothing().when(notificationService).markNotificationAsRead("123");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/notifications/123/mark-as-read"))
                .andExpect(status().isOk());

        // Verify
        verify(notificationService, times(1)).markNotificationAsRead("123");
    }
}