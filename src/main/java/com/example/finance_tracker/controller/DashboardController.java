package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Dashboard;
import com.example.finance_tracker.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard Controller", description = "APIs for fetching role-based dashboard data")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService; // Use the interface here

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get admin dashboard summary",
            description = "Fetch system-wide financial summary for admin users."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin dashboard data retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Dashboard> getAdminDashboard() {
        Dashboard summary = dashboardService.getAdminDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id" )
    @Operation(
            summary = "Get user dashboard summary",
            description = "Fetch personalized financial summary for regular users."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User dashboard data retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Dashboard> getUserDashboard(
            @Parameter(description = "ID of the user to fetch dashboard data for", required = true)
            @PathVariable String userId) {
        Dashboard summary = dashboardService.getUserDashboardSummary(userId);
        return ResponseEntity.ok(summary);
    }
}