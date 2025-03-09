package com.example.finance_tracker.service;

import com.example.finance_tracker.model.*;
import com.example.finance_tracker.repository.*;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service("goalService")
public class GoalsAndSavingsServiceImpl implements GoalsAndSavingsService {

    private final GoalRepository goalRepository;
    private final TransactionService transactionService;
    private final NotificationService notificationService;
    private final BudgetRepository budgetRepository;

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    private final UserRepository userRepository;
    @Autowired
    public GoalsAndSavingsServiceImpl(GoalRepository goalRepository, TransactionService transactionService,
                                      NotificationService notificationService, BudgetRepository budgetRepository,
                                      ExpenseService expenseService, IncomeService incomeService, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
        this.budgetRepository = budgetRepository;
        this.expenseService = expenseService;
        this.incomeService = incomeService;
        this.userRepository = userRepository;
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
        if (goal.getCurrentAmount() == 0) {
            goal.setCurrentAmount(0.0);
        }
        if (goal.getProgressPercentage() == 0) {
            goal.setProgressPercentage(0.0);
        }

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
    public List<Goal> getGoalsByUser(String userId) {
        return goalRepository.findByUserId(userId);
    }

    @Override
    public Goal getGoalById(String goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
    }

    @Override
    public Goal addManualContribution(String goalId, double amount) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        // Add the manual contribution
        goal.setManualContribution(goal.getManualContribution() + amount);

        // Update the current amount and progress percentage
        double totalSavings = goal.getCurrentAmount() + amount;
        goal.setCurrentAmount(totalSavings);

        double progressPercentage = (totalSavings / goal.getTargetAmount()) * 100;
        goal.setProgressPercentage(progressPercentage);

        // Save the updated goal
        return goalRepository.save(goal);
    }


    @Override
    public Goal trackGoalProgress(String goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        // Fetch the budget allocated to this goal (if any)
        Budget budget = budgetRepository.findByGoalId(goalId)
                .orElse(null);

        // Calculate total savings allocated to this goal from transactions
        double totalSavingsFromTransactions = transactionService.getTransactionsByUser(goal.getUserId()).stream()
                .filter(transaction -> transaction.getCategory().equals("Goals") && transaction.getGoalId() != null && transaction.getGoalId().equals(goalId))
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Calculate total savings allocated to this goal from the budget (if a budget is linked)
        double totalSavingsFromBudget = (budget != null) ? budget.getLimit() : 0;

        // Include manual contributions (if any)
        double totalSavingsFromManualContributions = goal.getManualContribution();

        // Update the current amount
        double totalSavings = totalSavingsFromTransactions + totalSavingsFromBudget + totalSavingsFromManualContributions;
        goal.setCurrentAmount(totalSavings);

        // Calculate progress percentage
        double progressPercentage = (totalSavings / goal.getTargetAmount()) * 100;
        goal.setProgressPercentage(progressPercentage);

        // Save the updated goal
        goalRepository.save(goal);

        // Notify user if the goal is achieved
        if (progressPercentage >= 100) {
            notifyUserAboutGoalProgress(goal, "Goal Achieved", "Congratulations! You have achieved your goal: " + goal.getName());
        }

        // Notify user if the goal is nearing its deadline
        LocalDate today = LocalDate.now();

        // Convert java.util.Date to LocalDate
        LocalDate deadline = goal.getDeadline().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        long daysRemaining = ChronoUnit.DAYS.between(today, deadline);

        if (daysRemaining <= 7 && daysRemaining > 0) {
            String message = String.format(
                    "Your goal '%s' is nearing its deadline. Only %d days remaining!",
                    goal.getName(), daysRemaining
            );
            notifyUserAboutGoalProgress(goal, "Goal Nearing Deadline", message);
        }

        return goal;
    }

    //------------------------------ Savings ------------------------------//

    @Override
    public double calculateNetSavings(String userId, Date startDate, Date endDate) {
        // Fetch converted total income to base currency
        double totalIncome = incomeService.calculateTotalIncomeInBaseCurrency(userId);

        // Fetch converted total expenses to base currency
        double totalExpenses = expenseService.calculateTotalExpensesInBaseCurrency(userId);

        // Calculate net savings
        double netSavings = totalIncome - totalExpenses;

        // Allocate net savings to goals
//        if (netSavings > 0) {
//            allocateSavings(userId, netSavings);
//        }

        return netSavings ;
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
                notifyUserAboutGoalProgress(goal, "Goal Nearing Deadline", message);
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

    @Override
    public boolean isOwner(String goalId, String userId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        return goal.getUserId().equals(userId);
    }

    private Notification createNotification(String userId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        return notification;
    }

    /**
     * Helper method to notify the user about goal progress.
     *
     * @param goal    The goal for which the notification is being sent.
     * @param title   The title of the notification.
     * @param message The message of the notification.
     */
    private void notifyUserAboutGoalProgress(Goal goal, String title, String message) {

        User user = userRepository.findById(goal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String userEmail = user.getEmail();

        Notification notification = new Notification();
        notification.setUserId(goal.getUserId());
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("GOAL_ALERT");
        notification.setCreatedAt(new Date());
        notification.setEmail(userEmail);

        // Send the notification
        notificationService.sendNotification(notification);
        notificationService.sendEmailNotification(notification);
    }

}