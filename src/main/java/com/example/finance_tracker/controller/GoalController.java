package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Goal> setGoal(@RequestBody Goal goal) {
        Goal newGoal = goalService.setGoal(goal);
        return ResponseEntity.ok(newGoal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Goal> updateGoal(@PathVariable String id, @RequestBody Goal goal) {
        goal.setId(id); // Ensure the ID matches the path variable
        Goal updatedGoal = goalService.updateGoal(goal);
        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable String id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/track-progress")
    public ResponseEntity<Void> trackGoalProgress(@PathVariable String id) {
        goalService.trackGoalProgress(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{userId}/allocate-savings")
    public ResponseEntity<Void> allocateSavings(@PathVariable String userId, @RequestParam double amount) {
        goalService.allocateSavings(userId, amount);
        return ResponseEntity.ok().build();
    }
}