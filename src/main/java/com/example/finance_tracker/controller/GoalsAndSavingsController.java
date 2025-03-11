package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.service.GoalsAndSavingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@Tag(name = "Goals and Savings Controller", description = "APIs for managing financial goals and savings")
public class GoalsAndSavingsController {

    private final GoalsAndSavingsService goalsAndSavingsService;

    @Autowired
    public GoalsAndSavingsController(GoalsAndSavingsService goalsAndSavingsService) {
        this.goalsAndSavingsService = goalsAndSavingsService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Set a new financial goal",
            description = "Create a new financial goal for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid goal data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> setGoal(
            @Parameter(description = "Goal details to create", required = true)
            @RequestBody Goal goal,
            @Parameter(description = "Authenticated user ID", required = true)
            @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        goal.setUserId(authenticatedUserId);
        Goal newGoal = goalsAndSavingsService.setGoal(goal);
        return ResponseEntity.ok().body("Goal created successfully: " + newGoal);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Update an existing goal",
            description = "Update a financial goal by ID. Only the owner of the goal can update it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid goal data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateGoal(
            @Parameter(description = "ID of the goal to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated goal details", required = true)
            @RequestBody Goal goal,
            @Parameter(description = "Authenticated user ID", required = true)
            @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        goal.setId(id);
        goal.setUserId(authenticatedUserId);
        Goal updatedGoal = goalsAndSavingsService.updateGoal(goal);
        return ResponseEntity.ok().body("Goal updated successfully: " + updatedGoal);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Delete a goal",
            description = "Delete a financial goal by ID. Only the owner of the goal can delete it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Goal deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteGoal(
            @Parameter(description = "ID of the goal to delete", required = true)
            @PathVariable String id) {
        goalsAndSavingsService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get all goals for a user",
            description = "Retrieve all financial goals for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goals retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getGoalsByUser(
            @Parameter(description = "ID of the user to retrieve goals for", required = true)
            @PathVariable String userId) {
        List<Goal> goals = goalsAndSavingsService.getGoalsByUser(userId);
        if (goals.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body("No goals found for user: " + userId);
        }
        return ResponseEntity.ok().body("Goals retrieved successfully: " + goals);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Get a goal by ID",
            description = "Retrieve a specific financial goal by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getGoalById(
            @Parameter(description = "ID of the goal to retrieve", required = true)
            @PathVariable String id) {
        Goal goal = goalsAndSavingsService.getGoalById(id);
        return ResponseEntity.ok().body("Goal retrieved successfully: " + goal);
    }

    @PostMapping("/{id}/add-contribution")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Add a manual contribution to a goal",
            description = "Add a manual contribution to a specific financial goal."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contribution added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> addManualContribution(
            @Parameter(description = "ID of the goal to add contribution to", required = true)
            @PathVariable String id,
            @Parameter(description = "Amount to contribute", required = true)
            @RequestParam double amount) {
        Goal updatedGoal = goalsAndSavingsService.addManualContribution(id, amount);
        return ResponseEntity.ok().body("Contribution added successfully: " + updatedGoal);
    }

    @PostMapping("/{id}/track-progress")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Track progress for a goal",
            description = "Track the progress of a specific financial goal."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress tracked successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> trackGoalProgress(
            @Parameter(description = "ID of the goal to track progress for", required = true)
            @PathVariable String id) {
        Goal updatedGoal = goalsAndSavingsService.trackGoalProgress(id);
        return ResponseEntity.ok().body("Progress tracked successfully: " + updatedGoal);
    }

    @GetMapping("/{id}/remaining-amount")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Calculate remaining amount for a goal",
            description = "Calculate the remaining amount needed to achieve a specific financial goal."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Remaining amount calculated successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Goal not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> calculateRemainingAmountForGoal(
            @Parameter(description = "ID of the goal to calculate remaining amount for", required = true)
            @PathVariable String id) {
        double remainingAmount = goalsAndSavingsService.calculateRemainingAmountForGoal(id);
        return ResponseEntity.ok().body("Remaining amount calculated: " + remainingAmount);
    }

    @GetMapping("/user/{userId}/active")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get active goals for a user",
            description = "Retrieve all active financial goals for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active goals retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getActiveGoals(
            @Parameter(description = "ID of the user to retrieve active goals for", required = true)
            @PathVariable String userId) {
        List<Goal> activeGoals = goalsAndSavingsService.getActiveGoals(userId);
        if (activeGoals.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body("No active goals found for user: " + userId);
        }
        return ResponseEntity.ok().body("Active goals retrieved successfully: " + activeGoals);
    }

    @GetMapping("/user/{userId}/completed")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get completed goals for a user",
            description = "Retrieve all completed financial goals for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed goals retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCompletedGoals(
            @Parameter(description = "ID of the user to retrieve completed goals for", required = true)
            @PathVariable String userId) {
        List<Goal> completedGoals = goalsAndSavingsService.getCompletedGoals(userId);
        if (completedGoals.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body("No completed goals found for user: " + userId);
        }
        return ResponseEntity.ok().body("Completed goals retrieved successfully: " + completedGoals);
    }

    @GetMapping("/user/{userId}/overdue")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get overdue goals for a user",
            description = "Retrieve all overdue financial goals for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue goals retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No overdue goals found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getOverdueGoals(
            @Parameter(description = "ID of the user to retrieve overdue goals for", required = true)
            @PathVariable String userId) {
        List<Goal> overdueGoals = goalsAndSavingsService.getOverdueGoals(userId);
        if (overdueGoals.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body("No overdue goals found for user: " + userId);
        }
        return ResponseEntity.ok().body("Overdue goals retrieved successfully: " + overdueGoals);
    }

    @PostMapping("/check-near-overdue")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Check and notify near-overdue goals",
            description = "Check for goals that are near their due date and notify users (Admin only)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Near-overdue goals checked successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> checkNearOverdueGoals() {
        goalsAndSavingsService.checkAndNotifyNearOverdueGoals();
        return ResponseEntity.ok().body("Near-overdue goals checked and notifications sent successfully.");
    }

    @PostMapping("/{goalId}/link-budget")
    @PreAuthorize("@goalService.isOwner(#goalId, authentication.principal.id)")
    @Operation(
            summary = "Link a budget to a goal",
            description = "Link a budget to a specific financial goal."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget linked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid budget ID provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Goal or budget not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> linkBudgetToGoal(
            @Parameter(description = "ID of the goal to link budget to", required = true)
            @PathVariable String goalId,
            @Parameter(description = "ID of the budget to link", required = true)
            @RequestParam String budgetId) {
        goalsAndSavingsService.linkBudgetToGoal(goalId, budgetId);
        return ResponseEntity.ok().body("Budget linked successfully to goal: " + goalId);
    }

    @PostMapping("/net-savings/{userId}/")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Calculate net savings for a user",
            description = "Calculate the net savings for a specific user within a date range."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Net savings calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> calculateNetSavings(
            @Parameter(description = "ID of the user to calculate net savings for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam String startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam String endDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            double netSavings = goalsAndSavingsService.calculateNetSavings(userId, start, end);
            return ResponseEntity.ok().body("Net savings calculated: " + netSavings);
        } catch (ParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use yyyy-MM-dd.");
        }
    }

    @PostMapping("/user/{userId}/allocate-savings")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Allocate savings to active goals",
            description = "Allocate a specified amount of savings to active financial goals for a user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Savings allocated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> allocateSavings(
            @Parameter(description = "ID of the user to allocate savings for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Amount to allocate", required = true)
            @RequestParam double amount) {
        goalsAndSavingsService.allocateSavings(userId, amount);
        return ResponseEntity.ok().body("Savings allocated successfully to active goals.");
    }

    @GetMapping("/user/{userId}/total-savings")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Calculate total savings for a user",
            description = "Calculate the total savings for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total savings calculated successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> calculateTotalSavings(
            @Parameter(description = "ID of the user to calculate total savings for", required = true)
            @PathVariable String userId) {
        double totalSavings = goalsAndSavingsService.calculateTotalSavings(userId);
        return ResponseEntity.ok().body("Total savings calculated: " + totalSavings);
    }
}