package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Date;

@Service("budgetService")
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;
    private final ExpenseRepository expenseRepository;
    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository, NotificationService notificationService, ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.notificationService = notificationService;
        this.expenseRepository = expenseRepository;
    }
    @Override
    public Budget setBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public Budget updateBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public boolean deleteBudget(String budgetId) {
        budgetRepository.deleteById(budgetId);
        return true;
    }

    @Override
    public List<Budget> getBudgetsByUser(String userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    public void checkBudgetExceeded(String userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        for (Budget budget : budgets) {
            // Fetch expenses for the current month and year
            List<Expense> expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(
                    userId,
                    budget.getCategory(),
                    LocalDate.of(currentYear, currentMonth, 1),
                    LocalDate.of(currentYear, currentMonth, now.lengthOfMonth())
            );

            // Calculate total expenses for the budget category
            double totalExpenses = expenses.stream()
                    .mapToDouble(Expense::getAmount)
                    .sum();

            // Check if the budget is exceeded
            if (totalExpenses > budget.getLimit()) {
                String message = String.format("Your budget for %s has been exceeded. Total spent: %.2f, Budget: %.2f",
                        budget.getCategory(), totalExpenses, budget.getLimit());

                // Send notification
                notificationService.sendNotification(userId, "Budget Exceeded", message);

//                // Optional: Send email notification
//                notificationService.sendEmailNotification(userId, "Budget Exceeded", message);
            }
        }
    }


    public boolean isOwner(String budgetId, String userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        return budget.getUserId().equals(userId); // Check if the user owns the budget
    }


}
