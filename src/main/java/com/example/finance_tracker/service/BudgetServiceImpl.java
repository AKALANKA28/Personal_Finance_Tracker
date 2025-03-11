package com.example.finance_tracker.service;

import com.example.finance_tracker.model.*;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("budgetService")
public class BudgetServiceImpl implements BudgetService {

    private static final Logger logger = LoggerFactory.getLogger(BudgetServiceImpl.class);

    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;
    private final ExpenseRepository expenseRepository;
    private final CurrencyConverterImpl currencyConverterImpl;
    private final GoalsAndSavingsService goalsAndSavingsService;
    private final CurrencyUtil currencyUtil;

    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository, NotificationService notificationService,
                             ExpenseRepository expenseRepository, CurrencyConverterImpl currencyConverterImpl,
                             GoalsAndSavingsService goalsAndSavingsService, CurrencyUtil currencyUtil) {
        this.budgetRepository = budgetRepository;
        this.notificationService = notificationService;
        this.expenseRepository = expenseRepository;
        this.currencyConverterImpl = currencyConverterImpl;
        this.goalsAndSavingsService = goalsAndSavingsService;
        this.currencyUtil = currencyUtil;
    }

    @Override
    public Budget setBudget(Budget budget) {
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(budget.getUserId());
        budget.setCurrencyCode(baseCurrency);
        budget.setStartDate(new Date());

        logger.info("Setting budget for user: {}", budget.getUserId());
        Budget savedBudget = budgetRepository.save(budget);
        logger.debug("Budget saved successfully: {}", savedBudget);
        return savedBudget;
    }

    @Override
    public Budget updateBudget(Budget budget) {
        budget.setId(budget.getId());
        logger.info("Updating budget with ID: {}", budget.getId());
        Budget updatedBudget = budgetRepository.save(budget);
        logger.debug("Budget updated successfully: {}", updatedBudget);
        return updatedBudget;
    }

    @Override
    public boolean deleteBudget(String budgetId) {
        logger.info("Deleting budget with ID: {}", budgetId);
        budgetRepository.deleteById(budgetId);
        logger.debug("Budget deleted successfully: {}", budgetId);
        return true;
    }

    @Override
    public List<Budget> getBudgetsByUser(String userId) {
        logger.info("Fetching budgets for user: {}", userId);
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        logger.debug("Fetched {} budgets for user: {}", budgets.size(), userId);
        return budgets;
    }

    @Override
    public void checkBudgetExceeded(String userId) {
        logger.info("Checking if budget is exceeded for user: {}", userId);

        List<Budget> budgets = budgetRepository.findByUserId(userId);
        logger.debug("Fetched {} budgets for user: {}", budgets.size(), userId);

        Date now = new Date(); // Current date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);
        logger.debug("Base currency for user {}: {}", userId, baseCurrency);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        // Set the end date to the last day of the current month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        logger.debug("Checking budget for period: {} to {}", startDate, endDate);

        for (Budget budget : budgets) {
            String category = budget.getCategory();
            logger.debug("Processing budget for category: {}", category);

            // Fetch expenses for the current month and year
            List<Expense> expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(
                    userId, category, startDate, endDate);
            logger.debug("Fetched {} expenses for category: {}", expenses.size(), category);

            // Calculate total expenses in the budget's currency
            double totalExpenses = expenses.stream()
                    .mapToDouble(expense -> currencyConverterImpl.convertCurrency(
                            expense.getCurrencyCode(),
                            budget.getCurrencyCode(),
                            expense.getAmount(),
                            baseCurrency
                    ))
                    .sum();

            logger.debug("Total expenses for category {}: {}", category, totalExpenses);

            // Calculate the percentage of the budget used
            double budgetUsed = totalExpenses / budget.getLimit();
            logger.debug("Budget used for category {}: {}%", category, budgetUsed * 100);

            // Notify if nearing or exceeding the budget
            if (budgetUsed >= 0.8 && budgetUsed < 1.0) {
                String message = String.format("You are nearing your budget for %s. Total spent: %.2f %s, Budget: %.2f %s",
                        category, totalExpenses, budget.getCurrencyCode(), budget.getLimit(), budget.getCurrencyCode());

                logger.info("Budget nearing limit for category {}: {}", category, message);

                Notification notification = createNotification(userId, "Budget Nearing Limit", message);
                notificationService.sendNotification(notification);
            } else if (budgetUsed >= 1.0) {
                String message = String.format("Your budget for %s has been exceeded. Total spent: %.2f %s, Budget: %.2f %s",
                        category, totalExpenses, budget.getCurrencyCode(), budget.getLimit(), budget.getCurrencyCode());

                logger.info("Budget exceeded for category {}: {}", category, message);

                Notification notification = createNotification(userId, "Budget Exceeded", message);
                notificationService.sendNotification(notification);
            }
        }
    }

    @Override
    public void provideBudgetAdjustmentRecommendations(String userId) {
        logger.info("Providing budget adjustment recommendations for user: {}", userId);

        // Get the current date
        Date now = new Date();

        // Calculate the date 3 months ago
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -3);
        Date threeMonthsAgo = calendar.getTime();

        logger.debug("Calculating recommendations for period: {} to {}", threeMonthsAgo, now);

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);
        logger.debug("Base currency for user {}: {}", userId, baseCurrency);

        // Fetch budgets, expenses, and income
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        logger.debug("Fetched {} budgets for user: {}", budgets.size(), userId);

        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, threeMonthsAgo, now);
        logger.debug("Fetched {} expenses for user: {}", expenses.size(), userId);

        // Calculate net savings
        double netSavings = goalsAndSavingsService.calculateNetSavings(userId, now, threeMonthsAgo);
        logger.debug("Net savings for user {}: {}", userId, netSavings);

        // Generate recommendations
        List<String> recommendations = new ArrayList<>();
        for (Budget budget : budgets) {
            String category = budget.getCategory();
            logger.debug("Processing budget for category: {}", category);

            // Calculate total spending for the budget category in the base currency
            double totalSpending = expenses.stream()
                    .filter(expense -> expense.getCategory().equals(category))
                    .mapToDouble(expense -> currencyConverterImpl.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount(), baseCurrency))
                    .sum();

            logger.debug("Total spending for category {}: {}", category, totalSpending);

            // Convert the budget limit to the base currency
            double budgetLimit = currencyConverterImpl.convertToBaseCurrency(budget.getCurrencyCode(), budget.getLimit(), baseCurrency);
            logger.debug("Budget limit for category {}: {}", category, budgetLimit);

            if (totalSpending > budgetLimit * 1.1) {
                String recommendation = String.format(
                        "Consider increasing your budget for %s. Average spending: %.2f %s, Current budget: %.2f %s",
                        category, totalSpending, baseCurrency, budgetLimit, baseCurrency
                );
                recommendations.add(recommendation);
                logger.info("Recommendation for category {}: {}", category, recommendation);
            } else if (totalSpending < budgetLimit * 0.9) {
                String recommendation = String.format(
                        "Consider decreasing your budget for %s. Average spending: %.2f %s, Current budget: %.2f %s",
                        category, totalSpending, baseCurrency, budgetLimit, baseCurrency
                );
                recommendations.add(recommendation);
                logger.info("Recommendation for category {}: {}", category, recommendation);
            }
        }

        // Add net savings to recommendations
        String netSavingsMessage = String.format("Your net savings over the last 3 months: %.2f %s",
                netSavings, baseCurrency);
        recommendations.add(netSavingsMessage);
        logger.info("Net savings recommendation: {}", netSavingsMessage);

        // Notify the user with recommendations
        if (!recommendations.isEmpty()) {
            String message = "Budget adjustment recommendations:\n" + String.join("\n", recommendations);
            logger.info("Sending budget adjustment recommendations to user: {}", userId);

            Notification notification = createNotification(userId, "Budget Adjustment Recommendations", message);
            notificationService.sendNotification(notification);
        }
    }

    @Override
    public void allocateBudgetToGoal(String userId, String goalId, double amount) {
        logger.info("Allocating budget to goal for user: {}, goal ID: {}, amount: {}", userId, goalId, amount);

        Goal goal = goalsAndSavingsService.getGoalById(goalId);
        Budget budget = (Budget) budgetRepository.findByUserIdAndCategory(userId, "Savings")
                .orElseThrow(() -> new ResourceNotFoundException("Savings budget not found"));

        // Ensure the allocation does not exceed the budget limit
        if (amount > budget.getLimit()) {
            logger.error("Allocation amount exceeds budget limit for user: {}", userId);
            throw new IllegalArgumentException("Allocation amount exceeds budget limit");
        }

        // Deduct the allocated amount from the budget
        budget.setLimit(budget.getLimit() - amount);
        budgetRepository.save(budget);
        logger.debug("Budget updated after allocation: {}", budget);

        // Add the allocated amount to the goal
        goal.setCurrentAmount(goal.getCurrentAmount() + amount);
        goalsAndSavingsService.updateGoal(goal);
        logger.debug("Goal updated after allocation: {}", goal);

        // Notify the user
        String message = String.format("Allocated %.2f %s from your budget to the goal '%s'",
                amount, budget.getCurrencyCode(), goal.getName());
        logger.info("Sending allocation notification to user: {}", userId);

        Notification notification = createNotification(userId, "Budget Allocation", message);
        notificationService.sendNotification(notification);
    }

    @Override
    public boolean isOwner(String budgetId, String userId) {
        logger.info("Checking if user {} owns budget {}", userId, budgetId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        boolean isOwner = budget.getUserId().equals(userId);
        logger.debug("User {} owns budget {}: {}", userId, budgetId, isOwner);

        return isOwner;
    }

    private Notification createNotification(String userId, String title, String message) {
        logger.debug("Creating notification for user: {}, title: {}, message: {}", userId, title, message);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("BUDGET_ALERT");
        notification.setRead(false);

        return notification;
    }
}