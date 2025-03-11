package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Budget;

import java.time.LocalDate;
import java.util.List;

public interface BudgetService {
    Budget setBudget(Budget budget);
    Budget updateBudget(Budget budget);
    boolean deleteBudget(String budgetId);
    List<Budget> getBudgetsByUser(String userId);
    void checkBudgetExceeded(String userId);
    void provideBudgetAdjustmentRecommendations(String userId);

//    double calculateNetSavings(String userId, LocalDate startDate, LocalDate endDate);

    void allocateBudgetToGoal(String userId, String goalId, double amount);

    boolean isOwner(String budgetId, String userId);
}
