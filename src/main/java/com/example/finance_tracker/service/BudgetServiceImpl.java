package com.example.finance_tracker.service;

import com.example.finance_tracker.model.*;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service("budgetService")
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CurrencyService currencyService;
    private final GoalService goalService;

    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository, NotificationService notificationService,
                             ExpenseRepository expenseRepository, IncomeRepository incomeRepository, CurrencyService currencyService, GoalService goalService) {
        this.budgetRepository = budgetRepository;
        this.notificationService = notificationService;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.currencyService = currencyService;
        this.goalService = goalService;
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

        LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate endDate = LocalDate.of(currentYear, currentMonth, now.lengthOfMonth());

        for (Budget budget : budgets) {
            // Fetch expenses for the current month and year
            List<Expense> expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(
                    userId, budget.getCategory(), startDate, endDate);

            // Calculate total expenses in the budget's currency
            double totalExpenses = expenses.stream()
                    .mapToDouble(expense -> currencyService.convertCurrency(
                            userId,
                            expense.getCurrencyCode(),
                            budget.getCurrencyCode(),
                            expense.getAmount()
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
        LocalDate now = LocalDate.now();
        LocalDate threeMonthsAgo = now.minusMonths(3);

        // Fetch budgets, expenses, and income
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, threeMonthsAgo, now);
        List<Income> incomes = incomeRepository.findByUserIdAndDateBetween(userId, threeMonthsAgo, now);

        // Convert all amounts to the base currency (e.g., USD)
        double totalIncome = incomes.stream()
                .mapToDouble(income -> currencyService.convertToBaseCurrency(income.getCurrencyCode(), income.getAmount()))
                .sum();

        double totalExpenses = expenses.stream()
                .mapToDouble(expense -> currencyService.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount()))
                .sum();

        // Calculate net savings
        double netSavings = totalIncome - totalExpenses;

        // Generate recommendations
        List<String> recommendations = new ArrayList<>();
        for (Budget budget : budgets) {
            // Calculate total spending for the budget category in the base currency
            double totalSpending = expenses.stream()
                    .filter(expense -> expense.getCategory().equals(budget.getCategory()))
                    .mapToDouble(expense -> currencyService.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount()))
                    .sum();

            // Convert the budget limit to the base currency
            double budgetLimit = currencyService.convertToBaseCurrency(budget.getCurrencyCode(), budget.getLimit());

            if (totalSpending > budgetLimit * 1.1) { // Spending exceeds budget by 10%
                recommendations.add(String.format("Consider increasing your budget for %s. Average spending: %.2f USD, Current budget: %.2f USD",
                        budget.getCategory(), totalSpending, budgetLimit));
            } else if (totalSpending < budgetLimit * 0.9) { // Spending is below budget by 10%
                recommendations.add(String.format("Consider decreasing your budget for %s. Average spending: %.2f USD, Current budget: %.2f USD",
                        budget.getCategory(), totalSpending, budgetLimit));
            }
        }

        // Add net savings to recommendations
        recommendations.add(String.format("Your net savings over the last 3 months: %.2f USD", netSavings));

        // Notify the user with recommendations
        if (!recommendations.isEmpty()) {
            String message = "Budget adjustment recommendations:\n" + String.join("\n", recommendations);

            Notification notification = createNotification(userId, "Budget Adjustment Recommendations", message);
            notificationService.sendNotification(notification);
        }
    }

    @Override
    public double calculateNetSavings(String userId, LocalDate startDate, LocalDate endDate) {
        // Fetch total income and convert to base currency
        double totalIncome = incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .mapToDouble(income -> currencyService.convertToBaseCurrency(income.getCurrencyCode(), income.getAmount()))
                .sum();

        // Fetch total expenses and convert to base currency
        double totalExpenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .mapToDouble(expense -> currencyService.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount()))
                .sum();

        // Calculate net savings
        return totalIncome - totalExpenses;
    }


    @Override
    public void allocateBudgetToGoal(String userId, String goalId, double amount) {
        Goal goal = goalService.getGoalById(goalId);
        Budget budget = (Budget) budgetRepository

                .findByUserIdAndCategory(userId, "Savings")
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
        goalService.updateGoal(goal);

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
