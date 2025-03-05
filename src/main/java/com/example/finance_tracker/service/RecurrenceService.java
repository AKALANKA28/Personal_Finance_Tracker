package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Transaction;

import java.util.Date;

public interface RecurrenceService {
    void processRecurringTransactions();
    boolean shouldProcessTransaction(Transaction transaction);
    boolean isSameDayOfWeek(Date date1, Date date2);
    boolean isSameDayOfMonth(Date date1, Date date2);
    boolean isRecurrenceEnded(Transaction transaction);
    Transaction createRecurringTransaction(Transaction transaction);
    Date calculateNextRecurrenceDate(Transaction transaction);
    void sendUpcomingTransactionNotification(Transaction transaction);
}