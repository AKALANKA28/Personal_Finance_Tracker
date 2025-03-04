package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Expense;

import java.util.List;

public interface ExpenseService {
    Expense addExpense(Expense expense);
    Expense updateExpense(Expense expense);
    boolean deleteExpense(String id);
    List<Expense> getExpensesByUser(String userId);
    List<Expense> getExpensesByUserAndCategory(String userId, String category);
    boolean isOwner(String expenseId, String userId);
}