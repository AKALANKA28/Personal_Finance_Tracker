package com.example.finance_tracker.model;

import lombok.Data;

import java.util.List;

@Data
public class Dashboard {
    private int totalUsers;// For Admin
    private double totalIncome;
    private double totalExpenses;

    private List<Transaction> recentTransactions; // For user
    private List<Budget> budgets;
    private List<Goal> goals;
    private double netSavings;
}