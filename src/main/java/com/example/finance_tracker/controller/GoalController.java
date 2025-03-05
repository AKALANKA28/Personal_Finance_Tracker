package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    @Autowired
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_USER')") // Only authenticated users with ROLE_USER can access
    public ResponseEntity<Goal> setGoal(@RequestBody Goal goal, @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        goal.setUserId(authenticatedUserId); // Set the user ID from the authenticated user
        Goal newGoal = goalService.setGoal(goal);
        return ResponseEntity.ok(newGoal);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)") // Ensure the user owns the goal
    public ResponseEntity<Goal> updateGoal(@PathVariable String id, @RequestBody Goal goal) {
        goal.setId(id); // Ensure the ID matches the path variable
        Goal updatedGoal = goalService.updateGoal(goal);
        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)") // Ensure the user owns the goal
    public ResponseEntity<Void> deleteGoal(@PathVariable String id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/track-progress")
    @PreAuthorize("@goalService.isOwner(#id, authentication.principal.id)") // Ensure the user owns the goal
    public ResponseEntity<Void> trackGoalProgress(@PathVariable String id) {
        goalService.trackGoalProgress(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{userId}/allocate-savings")
    @PreAuthorize("#userId == authentication.principal.id") // Users can only allocate savings for themselves
    public ResponseEntity<Void> allocateSavings(@PathVariable String userId, @RequestParam double amount) {
        goalService.allocateSavings(userId, amount);
        return ResponseEntity.ok().build();
    }
}