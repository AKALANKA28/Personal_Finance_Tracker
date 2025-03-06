package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Notification;
import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class RecurrenceServiceImpl implements RecurrenceService {

    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Autowired
    public RecurrenceServiceImpl(TransactionRepository transactionRepository, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Scheduled(cron = "0 0 8 * * ?") // Runs every day at 8:00 AM
    public void processRecurringTransactions() {
        List<Transaction> recurringTransactions = transactionRepository.findByIsRecurring(true);

        for (Transaction transaction : recurringTransactions) {
            // Check if the transaction should be processed today
            if (shouldProcessTransaction(transaction)) {
                // Create a new transaction based on the recurrence pattern
                Transaction newTransaction = createRecurringTransaction(transaction);

                // Save the new transaction
                transactionRepository.save(newTransaction);

                // Send a notification for the upcoming transaction
                sendUpcomingTransactionNotification(newTransaction);
            }

            // Check if the recurrence has ended
            if (isRecurrenceEnded(transaction)) {
                // Mark the transaction as non-recurring
                transaction.setIsRecurring(false);
                transactionRepository.save(transaction);
            }
        }
    }

    @Override
    public boolean shouldProcessTransaction(Transaction transaction) {
        Date today = new Date();
        Date lastProcessedDate = transaction.getDate();

        switch (transaction.getRecurrencePattern()) {
            case "daily":
                return true; // Process every day
            case "weekly":
                return isSameDayOfWeek(today, lastProcessedDate);
            case "monthly":
                return isSameDayOfMonth(today, lastProcessedDate);
            default:
                return false;
        }
    }

    @Override
    public boolean isSameDayOfWeek(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.DAY_OF_WEEK) == cal2.get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public boolean isSameDayOfMonth(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public boolean isRecurrenceEnded(Transaction transaction) {
        if (transaction.getRecurrenceEndDate() == null) {
            return false; // No end date specified
        }
        return new Date().after(transaction.getRecurrenceEndDate());
    }

    @Override
    public Transaction createRecurringTransaction(Transaction transaction) {
        return Transaction.builder()
                .userId(transaction.getUserId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .currencyCode(transaction.getCurrencyCode())
                .category(transaction.getCategory())
                .source(transaction.getSource())
                .date(calculateNextRecurrenceDate(transaction)) // Update the date
                .description(transaction.getDescription())
                .tags(transaction.getTags())
                .IsRecurring(true)
                .recurrencePattern(transaction.getRecurrencePattern())
                .recurrenceEndDate(transaction.getRecurrenceEndDate())
                .build();
    }

    @Override
    public Date calculateNextRecurrenceDate(Transaction transaction) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getDate());

        switch (transaction.getRecurrencePattern()) {
            case "daily":
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case "weekly":
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "monthly":
                calendar.add(Calendar.MONTH, 1);
                break;
            default:
                throw new IllegalArgumentException("Invalid recurrence pattern: " + transaction.getRecurrencePattern());
        }

        return calendar.getTime();
    }

    @Override
    public void sendUpcomingTransactionNotification(Transaction transaction) {
        String message = String.format(
                "Upcoming recurring transaction: %s (%.2f %s) on %s",
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCurrencyCode(),
                transaction.getDate()
        );

        Notification notification = new Notification();
        notification.setUserId(transaction.getUserId());
        notification.setTitle("Upcoming Recurring Transaction");
        notification.setMessage(message);
        notificationService.sendNotification(notification);
    }
}