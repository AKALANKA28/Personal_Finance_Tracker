package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction addTransaction(Transaction transaction);
    Transaction updateTransaction(Transaction transaction);
    boolean deleteTransaction(String transactionId);
    List<Transaction> getTransactionsByUser(String userId);
    List<Transaction> getTransactionsByCategory(String userId, String category);
    List<Transaction> getTransactionsByTags(String userId, List<String> tags);
}
