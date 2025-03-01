package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Goal;

public interface GoalService {
    Goal setGoal(Goal goal);
    Goal updateGoal(Goal goal);
    boolean deleteGoal(String goalId);
    void trackGoalProgress(String goalId);
    void allocateSavings(String userId, double amount);
}
