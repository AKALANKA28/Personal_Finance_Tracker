package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.service.GoalsAndSavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalsAndSavingsController {

    private final GoalsAndSavingsService goalsAndSavingsService;

    @Autowired
    public GoalsAndSavingsController(GoalsAndSavingsService goalsAndSavingsService) {
        this.goalsAndSavingsService = goalsAndSavingsService;
    }

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Goal> setGoal(@RequestBody Goal goal, @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        goal.setUserId(authenticatedUserId);
        Goal newGoal = goalsAndSavingsService.setGoal(goal);
        return ResponseEntity.ok(newGoal);
    }

    // Update an existing goal
    @PutMapping("/{id}")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)") // Make sure the user is the owner of the goal
    public ResponseEntity<Goal> updateGoal(@PathVariable String id, @RequestBody Goal goal, @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        goal.setId(id);
        goal.setUserId(authenticatedUserId);

        Goal updatedGoal = goalsAndSavingsService.updateGoal(goal);
        return ResponseEntity.ok(updatedGoal);
    }

    // Delete a goal
    @DeleteMapping("/{id}")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Void> deleteGoal(@PathVariable String id) {
        goalsAndSavingsService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    // Get all goals for a user
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<List<Goal>> getGoalsByUser(@PathVariable String userId) {
        List<Goal> goals = goalsAndSavingsService.getGoalsByUser(userId);
        return ResponseEntity.ok(goals);
    }

    // Get a specific goal by ID
    @GetMapping("/{id}")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Goal> getGoalById(@PathVariable String id) {
        Goal goal = goalsAndSavingsService.getGoalById(id);
        return ResponseEntity.ok(goal);
    }

    /**
     * Add a manual contribution to a goal.
     *
     * @param id     The ID of the goal.
     * @param amount The amount to be added as a manual contribution.
     * @return The updated goal.
     */
    @PostMapping("/{id}/add-contribution")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Goal> addManualContribution(
            @PathVariable String id,
            @RequestParam double amount) {
        Goal updatedGoal = goalsAndSavingsService.addManualContribution(id, amount);
        return ResponseEntity.ok(updatedGoal);
    }

    // Track progress for a specific goal
    @PostMapping("/{id}/track-progress")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Goal> trackGoalProgress(@PathVariable String id) {
        Goal updatedGoal = goalsAndSavingsService.trackGoalProgress(id);
        return ResponseEntity.ok(updatedGoal);
    }

    // Calculate the remaining amount needed for a specific goal
    @GetMapping("/{id}/remaining-amount")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Double> calculateRemainingAmountForGoal(@PathVariable String id) {
        double remainingAmount = goalsAndSavingsService.calculateRemainingAmountForGoal(id);
        return ResponseEntity.ok(remainingAmount);
    }

    // Get all active goals for a user
    @GetMapping("/user/{userId}/active")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<List<Goal>> getActiveGoals(@PathVariable String userId) {
        List<Goal> activeGoals = goalsAndSavingsService.getActiveGoals(userId);
        return ResponseEntity.ok(activeGoals);
    }

    // Get all completed goals for a user
    @GetMapping("/user/{userId}/completed")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<List<Goal>> getCompletedGoals(@PathVariable String userId) {
        List<Goal> completedGoals = goalsAndSavingsService.getCompletedGoals(userId);
        return ResponseEntity.ok(completedGoals);
    }

    /// ////////////////////////////
    // Get all overdue goals for a user
    @GetMapping("/user/{userId}/overdue")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> getOverdueGoals(@PathVariable String userId) {
        List<Goal> overdueGoals = goalsAndSavingsService.getOverdueGoals(userId);

        if (overdueGoals.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body("No overdue goals found.");
        }

        return ResponseEntity.ok(overdueGoals);
    }

    @PostMapping("/check-near-overdue")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> checkNearOverdueGoals() {
        goalsAndSavingsService.checkAndNotifyNearOverdueGoals();
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{goalId}/link-budget")
    @PreAuthorize("@goalService.isOwner(#goalId, authentication.principal.id)") // Ensure the user owns the goal
    public ResponseEntity<Void> linkBudgetToGoal(
            @PathVariable String goalId,
            @RequestParam String budgetId) {
        goalsAndSavingsService.linkBudgetToGoal(goalId, budgetId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/net-savings/{userId}/")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Double> calculateNetSavings(
            @PathVariable String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        double netSavings = goalsAndSavingsService.calculateNetSavings(userId, start, end);
        return ResponseEntity.ok(netSavings);
    }

    // Allocate savings to active goals
    @PostMapping("/user/{userId}/allocate-savings")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<Void> allocateSavings(@PathVariable String userId, @RequestParam double amount) {
        goalsAndSavingsService.allocateSavings(userId, amount);
        return ResponseEntity.ok().build();
    }

    // Calculate total savings for a user
    @GetMapping("/user/{userId}/total-savings")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<Double> calculateTotalSavings(@PathVariable String userId) {
        double totalSavings = goalsAndSavingsService.calculateTotalSavings(userId);
        return ResponseEntity.ok(totalSavings);
    }




}