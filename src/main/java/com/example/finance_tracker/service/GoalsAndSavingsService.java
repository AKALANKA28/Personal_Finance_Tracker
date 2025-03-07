package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Goal;

import java.time.LocalDate;
import java.util.List;

public interface GoalsAndSavingsService {
    Goal setGoal(Goal goal);
    Goal updateGoal(Goal goal);

    boolean deleteGoal(String goalId);

    Goal trackGoalProgress(String goalId);

    Goal addManualContribution(String goalId, double amount);

    double calculateNetSavings(String userId, LocalDate startDate, LocalDate endDate);

    void allocateSavings(String userId, double amount);

//    void allocateSavingsFromIncome(String userId, double savingsPercentage);

    List<Goal> getGoalsByUser(String userId);

    Goal getGoalById(String goalId);

    boolean isOwner(String goalId, String userId);

    double calculateTotalSavings(String userId);

    double calculateRemainingAmountForGoal(String goalId);

    List<Goal> getActiveGoals(String userId);

    List<Goal> getCompletedGoals(String userId);

    void checkAndNotifyNearOverdueGoals();

    List<Goal> getOverdueGoals(String userId);

    void linkBudgetToGoal(String goalId, String budgetId);

    void unlinkBudgetFromGoal(String goalId);
}
