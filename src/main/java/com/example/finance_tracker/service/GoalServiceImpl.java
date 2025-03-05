package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.repository.GoalRepository;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service("goalService")
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    @Autowired
    public GoalServiceImpl(GoalRepository goalRepository, TransactionService transactionService,
                           NotificationService notificationService) {
        this.goalRepository = goalRepository;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
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

        // Calculate total savings allocated to this goal
        double totalSavings = transactionService.getTransactionsByUser(goal.getUserId()).stream()
                .filter(transaction -> transaction.getCategory().equals("Savings"))
                .mapToDouble(transaction -> transaction.getAmount())
                .sum();

        // Update the current amount
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
}