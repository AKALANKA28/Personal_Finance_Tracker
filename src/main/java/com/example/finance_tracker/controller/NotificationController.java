package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Controller", description = "APIs for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Send a notification",
            description = "Send a notification to a user. Only authenticated users can send notifications."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid notification data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> sendNotification(
            @Parameter(description = "Notification details to send", required = true)
            @RequestBody Notification notification,
            @Parameter(description = "Authenticated user ID", required = true)
            @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        notification.setUserId(authenticatedUserId);
        notificationService.sendNotification(notification);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get notifications by user ID",
            description = "Retrieve all notifications for a specific user. Users can only view their own notifications unless they are admins."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Notification>> getNotificationsByUser(
            @Parameter(description = "ID of the user to retrieve notifications for", required = true)
            @PathVariable String userId) {
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/mark-as-read")
    @PreAuthorize("@notificationService.isOwner(#id, authentication.principal.id) or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Mark a notification as read",
            description = "Mark a specific notification as read. Only the owner of the notification or an admin can mark it as read."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> markNotificationAsRead(
            @Parameter(description = "ID of the notification to mark as read", required = true)
            @PathVariable String id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }
}