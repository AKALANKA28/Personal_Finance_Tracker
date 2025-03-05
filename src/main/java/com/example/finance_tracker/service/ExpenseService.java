package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Expense;

import java.util.List;

public interface ExpenseService {
    Expense addExpense(Expense expense);
    Expense updateExpense(Expense expense);
    boolean deleteExpense(String id);
    List<Expense> getExpensesByUser(String userId);

    List<Expense> getExpensesByUserInPreferredCurrency(String userId, String preferredCurrency);

    Expense convertExpenseToPreferredCurrency(Expense expense, String preferredCurrency);

    List<Expense> getExpensesByUserAndCategory(String userId, String category);
    boolean isOwner(String expenseId, String userId);

    double calculateTotalExpensesInBaseCurrency(String userId);

    Expense getExpenseById(String expenseId);
}