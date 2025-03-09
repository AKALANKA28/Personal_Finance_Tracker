package com.example.finance_tracker.service;

import com.example.finance_tracker.model.*;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("budgetService")
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;
    private final ExpenseRepository expenseRepository;
    private final CurrencyConverterImpl currencyConverterImpl;
    private final GoalsAndSavingsService goalsAndSavingsService;
    private final CurrencyUtil currencyUtil;
    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository, NotificationService notificationService,
                             ExpenseRepository expenseRepository, CurrencyConverterImpl currencyConverterImpl, GoalsAndSavingsService goalsAndSavingsService, CurrencyUtil currencyUtil) {
        this.budgetRepository = budgetRepository;
        this.notificationService = notificationService;
        this.expenseRepository = expenseRepository;
        this.currencyConverterImpl = currencyConverterImpl;
        this.goalsAndSavingsService = goalsAndSavingsService;
        this.currencyUtil = currencyUtil;
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
        Date now = new Date(); // Current date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        // Set the end date to the last day of the current month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        for (Budget budget : budgets) {
            // Fetch expenses for the current month and year
            List<Expense> expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(
                    userId, budget.getCategory(), startDate, endDate);

            // Calculate total expenses in the budget's currency
            double totalExpenses = expenses.stream()
                    .mapToDouble(expense -> currencyConverterImpl.convertCurrency(
                            expense.getCurrencyCode(),
                            budget.getCurrencyCode(),
                            expense.getAmount(),
                            baseCurrency
                    ))
                    .sum();

            // Calculate the percentage of the budget used
            double budgetUsed = totalExpenses / budget.getLimit();

            // Notify if nearing or exceeding the budget
            if (budgetUsed >= 0.8 && budgetUsed < 1.0) {
                String message = String.format("You are nearing your budget for %s. Total spent: %.2f %s, Budget: %.2f %s",
                        budget.getCategory(), totalExpenses, budget.getCurrencyCode(), budget.getLimit(), budget.getCurrencyCode());

                Notification notification = createNotification(userId, "Budget Nearing Limit", message);
                notificationService.sendNotification(notification);
            } else if (budgetUsed >= 1.0) {
                String message = String.format("Your budget for %s has been exceeded. Total spent: %.2f %s, Budget: %.2f %s",
                        budget.getCategory(), totalExpenses, budget.getCurrencyCode(), budget.getLimit(), budget.getCurrencyCode());

                Notification notification = createNotification(userId, "Budget Exceeded", message);
                notificationService.sendNotification(notification);
            }
        }
    }

    @Override
    public void provideBudgetAdjustmentRecommendations(String userId) {
        // Get the current date
        Date now = new Date();

        // Calculate the date 3 months ago
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -3);
        Date threeMonthsAgo = calendar.getTime();

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);

        // Fetch budgets, expenses, and income
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, threeMonthsAgo, now);

        // Calculate net savings
        double netSavings = goalsAndSavingsService.calculateNetSavings(userId, now, threeMonthsAgo);

        // Generate recommendations
        List<String> recommendations = new ArrayList<>();
        for (Budget budget : budgets) {
            // Calculate total spending for the budget category in the base currency
            double totalSpending = expenses.stream()
                    .filter(expense -> expense.getCategory().equals(budget.getCategory()))
                    .mapToDouble(expense -> currencyConverterImpl.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount(), baseCurrency))
                    .sum();

            // Convert the budget limit to the base currency
            double budgetLimit = currencyConverterImpl.convertToBaseCurrency(budget.getCurrencyCode(), budget.getLimit(), baseCurrency);

            if (totalSpending > budgetLimit * 1.1) {
                recommendations.add(String.format(
                        "Consider increasing your budget for %s. Average spending: %.2f %s, Current budget: %.2f %s",
                        budget.getCategory(), totalSpending, baseCurrency, budgetLimit, baseCurrency
                ));
            } else if (totalSpending < budgetLimit * 0.9) {
                recommendations.add(String.format(
                        "Consider decreasing your budget for %s. Average spending: %.2f %s, Current budget: %.2f %s",
                        budget.getCategory(), totalSpending, baseCurrency, budgetLimit, baseCurrency
                ));
            }
        }

        // Add net savings to recommendations
        recommendations.add(String.format("Your net savings over the last 3 months: %.2f %s",
                netSavings, baseCurrency));

        // Notify the user with recommendations
        if (!recommendations.isEmpty()) {
            String message = "Budget adjustment recommendations:\n" + String.join("\n", recommendations);

            Notification notification = createNotification(userId, "Budget Adjustment Recommendations", message);
            notificationService.sendNotification(notification);
        }
    }

    @Override
    public void allocateBudgetToGoal(String userId, String goalId, double amount) {
        Goal goal = goalsAndSavingsService.getGoalById(goalId);
        Budget budget = (Budget) budgetRepository.findByUserIdAndCategory(userId, "Savings")
                .orElseThrow(() -> new ResourceNotFoundException("Savings budget not found"));

        // Ensure the allocation does not exceed the budget limit
        if (amount > budget.getLimit()) {
            throw new IllegalArgumentException("Allocation amount exceeds budget limit");
        }

        // Deduct the allocated amount from the budget
        budget.setLimit(budget.getLimit() - amount);
        budgetRepository.save(budget);

        // Add the allocated amount to the goal
        goal.setCurrentAmount(goal.getCurrentAmount() + amount);
        goalsAndSavingsService.updateGoal(goal);

        // Notify the user
        String message = String.format("Allocated %.2f %s from your budget to the goal '%s'",
                amount, budget.getCurrencyCode(), goal.getName());
        Notification notification = createNotification(userId, "Budget Allocation", message);
        notificationService.sendNotification(notification);
    }

    public boolean isOwner(String budgetId, String userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        return budget.getUserId().equals(userId); // Check if the user owns the budget
    }

    private Notification createNotification(String userId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("BUDGET_ALERT");
        notification.setRead(false);
        return notification;
    }
}