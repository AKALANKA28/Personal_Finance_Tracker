package com.example.finance_tracker.service;

import com.example.finance_tracker.model.*;
import com.example.finance_tracker.repository.*;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service("goalService")
public class GoalsAndSavingsServiceImpl implements GoalsAndSavingsService {

    private final GoalRepository goalRepository;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    private final BudgetRepository budgetRepository;
//    private final TransactionRepository transactionRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final CurrencyConverter currencyConverter;

    @Autowired
    public GoalsAndSavingsServiceImpl(GoalRepository goalRepository, TransactionService transactionService,
                                      NotificationService notificationService, BudgetRepository budgetRepository, IncomeRepository incomeRepository, ExpenseRepository expenseRepository, CurrencyConverter currencyConverter) {
        this.goalRepository = goalRepository;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
        this.budgetRepository = budgetRepository;
//        this.transactionRepository = transactionRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.currencyConverter = currencyConverter;
    }

    @Override
    public Goal setGoal(Goal goal) {
        // Validate the goal amount and deadline
        if (goal.getTargetAmount() <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than 0");
        }
        if (goal.getDeadline().before(new Date())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }

        // Set the current amount to 0 initially
        goal.setCurrentAmount(0);

        return goalRepository.save(goal);
    }

    @Override
    public Goal updateGoal(Goal goal) {
        // Ensure the goal exists
        if (!goalRepository.existsById(goal.getId())) {
            throw new ResourceNotFoundException("Goal not found");
        }

        // Validate the goal amount and deadline
        if (goal.getTargetAmount() <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than 0");
        }
        if (goal.getDeadline().before(new Date())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }

        return goalRepository.save(goal);
    }

    @Override
    public boolean deleteGoal(String goalId) {
        if (!goalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal not found");
        }
        goalRepository.deleteById(goalId);
        return true;
    }

    @Override
    public void trackGoalProgress(String goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        // Fetch the budget allocated to this goal (if any)
        Budget budget = budgetRepository.findByGoalId(goalId)
                .orElse(null);

        // Calculate total savings allocated to this goal from transactions
        double totalSavingsFromTransactions = transactionService.getTransactionsByUser(goal.getUserId()).stream()
                .filter(transaction -> transaction.getCategory().equals("Savings") && transaction.getGoalId() != null && transaction.getGoalId().equals(goalId))
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Calculate total savings allocated to this goal from the budget (if a budget is linked)
        double totalSavingsFromBudget = (budget != null) ? budget.getLimit() : 0;

        // Update the current amount
        double totalSavings = totalSavingsFromTransactions + totalSavingsFromBudget;
        goal.setCurrentAmount(totalSavings);

        // Calculate progress percentage
        double progressPercentage = (totalSavings / goal.getTargetAmount()) * 100;
        goal.setProgressPercentage(progressPercentage);

        // Save the updated goal
        goalRepository.save(goal);

        // Notify user if the goal is achieved
        if (progressPercentage >= 100) {
            Notification notification = new Notification();
            notification.setUserId(goal.getUserId());
            notification.setTitle("Goal Achieved");
            notification.setMessage("Congratulations! You have achieved your goal: " + goal.getName());
            notificationService.sendNotification(notification);
            notificationService.sendEmailNotification(notification);
        }

        // Notify user if the goal is nearing its deadline
        LocalDate today = LocalDate.now();
        LocalDate deadline = LocalDate.parse(goal.getDeadline().toString());
        long daysRemaining = ChronoUnit.DAYS.between(today, deadline);

        if (daysRemaining <= 7 && daysRemaining > 0) {
            String message = String.format(
                    "Your goal '%s' is nearing its deadline. Only %d days remaining!",
                    goal.getName(), daysRemaining
            );

            Notification notification = new Notification();
            notification.setUserId(goal.getUserId());
            notification.setTitle("Goal Nearing Deadline");
            notification.setMessage(message);
            notificationService.sendNotification(notification);
            notificationService.sendEmailNotification(notification);
        }
    }

    @Override
    public double calculateNetSavings(String userId, LocalDate startDate, LocalDate endDate) {
        // Fetch total income and convert to base currency
        double totalIncome = incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .mapToDouble(income -> currencyConverter.convertToBaseCurrency(income.getCurrencyCode(), income.getAmount()))
                .sum();

        // Fetch total expenses and convert to base currency
        double totalExpenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .mapToDouble(expense -> currencyConverter.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount()))
                .sum();

        // Calculate net savings
        double netSavings = totalIncome - totalExpenses;

        // Allocate net savings to goals
        if (netSavings > 0) {
            allocateSavings(userId, netSavings);
        }

        return netSavings;
    }

    @Override
    public void allocateSavings(String userId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // Fetch the user's active goals
        List<Goal> activeGoals = goalRepository.findByUserIdAndDeadlineAfter(userId, new Date());

        // Allocate savings proportionally to each goal based on their target amounts
        double totalTargetAmount = activeGoals.stream()
                .mapToDouble(Goal::getTargetAmount)
                .sum();

        for (Goal goal : activeGoals) {
            double allocation = (goal.getTargetAmount() / totalTargetAmount) * amount;
            transactionService.addTransaction(new Transaction(
                    userId, allocation, "Savings", LocalDate.now(), "Savings allocation for goal: " + goal.getName()
            ));

            // Update goal progress
            trackGoalProgress(goal.getId());
        }
    }
//    @Override
//    public void allocateSavingsFromIncome(String userId, double savingsPercentage) {
//        if (savingsPercentage <= 0 || savingsPercentage > 100) {
//            throw new IllegalArgumentException("Savings percentage must be between 0 and 100");
//        }
//
//        // Fetch the user's total income
//        double totalIncome = incomeRepository.findByUserId(userId).stream()
//                .mapToDouble(Income::getAmount)
//                .sum();
//
//        // Calculate the savings amount
//        double savingsAmount = (totalIncome * savingsPercentage) / 100;
//
//        // Fetch the user's active goals
//        List<Goal> activeGoals = goalRepository.findByUserIdAndDeadlineAfter(userId, new Date());
//
//        // Allocate savings proportionally to each goal based on their target amounts
//        double totalTargetAmount = activeGoals.stream()
//                .mapToDouble(Goal::getTargetAmount)
//                .sum();
//
//        for (Goal goal : activeGoals) {
//            double allocation = (goal.getTargetAmount() / totalTargetAmount) * savingsAmount;
//
//            // Create a savings transaction
//            Transaction savingsTransaction = getTransaction(userId, goal, allocation);
//
//            transactionRepository.save(savingsTransaction);
//
//            // Update goal progress
//            trackGoalProgress(goal.getId());
//        }
//    }

//    private static Transaction getTransaction(String userId, Goal goal, double allocation) {
//        Transaction savingsTransaction = new Transaction();
//        savingsTransaction.setUserId(userId);
//        savingsTransaction.setType("Expense");
//        savingsTransaction.setAmount(allocation);
//        savingsTransaction.setCurrencyCode("USD"); // Default currency
//        savingsTransaction.setCategory("Savings");
//        savingsTransaction.setDate(new Date());
//        savingsTransaction.setDescription("Savings allocation for goal: " + goal.getName());
//        savingsTransaction.setGoalId(goal.getId());
//        return savingsTransaction;
//    }

    @Override
    public List<Goal> getGoalsByUser(String userId) {
        return goalRepository.findByUserId(userId);
    }

    @Override
    public Goal getGoalById(String goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
    }

    public boolean isOwner(String goalId, String userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        return goal.getUserId().equals(userId); // Check if the user owns the goal
    }

    @Override
    public double calculateTotalSavings(String userId) {
        return transactionService.getTransactionsByUser(userId).stream()
                .filter(transaction -> transaction.getCategory().equals("Savings"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Override
    public double calculateRemainingAmountForGoal(String goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        double totalSavings = calculateTotalSavings(goal.getUserId());
        return goal.getTargetAmount() - totalSavings;
    }

    @Override
    public List<Goal> getActiveGoals(String userId) {
        return goalRepository.findByUserIdAndDeadlineAfter(userId, new Date());
    }

    @Override
    public List<Goal> getCompletedGoals(String userId) {
        return goalRepository.findByUserIdAndProgressPercentageGreaterThanEqual(userId, 100);
    }

    @Override
    public void checkAndNotifyNearOverdueGoals() {
        LocalDate today = LocalDate.now();
        List<Goal> activeGoals = goalRepository.findByDeadlineAfter(new Date()); // Fetch all active goals

        for (Goal goal : activeGoals) {
            LocalDate deadline = LocalDate.parse(goal.getDeadline().toString()); // Convert deadline to LocalDate
            long daysRemaining = ChronoUnit.DAYS.between(today, deadline);

            // Notify user if the goal is nearing overdue (e.g., 7 days or less remaining)
            if (daysRemaining <= 7 && daysRemaining > 0) {
                String message = String.format(
                        "Your goal '%s' is nearing its deadline. Only %d days remaining!",
                        goal.getName(), daysRemaining
                );

                Notification notification = new Notification();
                notification.setUserId(goal.getUserId());
                notification.setTitle("Goal Nearing Deadline");
                notification.setMessage(message);
                notificationService.sendNotification(notification);
                notificationService.sendEmailNotification(notification);
            }
        }
    }

    // Scheduled task to check for near-overdue goals daily
    @Scheduled(cron = "0 0 8 * * ?") // Runs every day at 8:00 AM
    public void scheduledCheckForNearOverdueGoals() {
        checkAndNotifyNearOverdueGoals();
    }
    @Override
    public List<Goal> getOverdueGoals(String userId) {
        return goalRepository.findByUserIdAndDeadlineBeforeAndProgressPercentageLessThan(userId, new Date(), 100);
    }

    @Override
    public void linkBudgetToGoal(String goalId, String budgetId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        // Ensure the budget and goal belong to the same user
        if (!budget.getUserId().equals(goal.getUserId())) {
            throw new IllegalArgumentException("Budget and goal must belong to the same user");
        }

        // Link the budget to the goal
        goal.setBudgetId(budgetId);
        goalRepository.save(goal);

        // Notify the user
        String message = String.format("Budget '%s' has been linked to goal '%s'", budget.getCategory(), goal.getName());
        Notification notification = createNotification(goal.getUserId(), "Budget Linked to Goal", message);
        notificationService.sendNotification(notification);
    }

    @Override
    public void unlinkBudgetFromGoal(String goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        // Unlink the budget from the goal
        goal.setBudgetId(null);
        goalRepository.save(goal);

        // Notify the user
        String message = String.format("Budget has been unlinked from goal '%s'", goal.getName());
        Notification notification = createNotification(goal.getUserId(), "Budget Unlinked from Goal", message);
        notificationService.sendNotification(notification);
    }



    private Notification createNotification(String userId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        return notification;
    }



}