package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Dashboard;
import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Goal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.finance_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Override
    public Dashboard getAdminDashboardSummary() {
        Dashboard summary = new Dashboard();
        long totalUsers = userRepository.count();
        logger.info("Total Users: {}", totalUsers); // Debug log
        summary.setTotalUsers((int) totalUsers);

        double totalIncome = calculateTotalIncome();
        logger.info("Total Income: {}", totalIncome); // Debug log
        summary.setTotalIncome(totalIncome);

        double totalExpenses = calculateTotalExpenses();
        logger.info("Total Expenses: {}", totalExpenses); // Debug log
        summary.setTotalExpenses(totalExpenses);

        return summary;
    }

    @Override
    public Dashboard getUserDashboardSummary(String userId) {
        Dashboard summary = new Dashboard();
        summary.setRecentTransactions(transactionRepository.findRecentTransactionsByUser(userId, 5));
        summary.setBudgets(budgetRepository.findByUserId(userId));
        summary.setGoals(goalRepository.findByUserId(userId));
        summary.setNetSavings(calculateNetSavings(userId));
        return summary;
    }

    private double calculateTotalIncome() {
        List<Transaction> incomeTransactions = transactionRepository.findAllIncomeTransactions();
        logger.info("Fetched {} income transactions for admin", incomeTransactions.size()); // Debug log
        return incomeTransactions.stream().mapToDouble(Transaction::getAmount).sum();
    }

    private double calculateTotalExpenses() {
        List<Transaction> expenseTransactions = transactionRepository.findAllExpenseTransactions();
        logger.info("Fetched {} expense transactions for admin", expenseTransactions.size()); // Debug log
        return expenseTransactions.stream().mapToDouble(Transaction::getAmount).sum();
    }

    private double calculateNetSavings(String userId) {
        List<Transaction> incomeTransactions = transactionRepository.findIncomeTransactionsByUser(userId);
        List<Transaction> expenseTransactions = transactionRepository.findExpenseTransactionsByUser(userId);

        double totalIncome = incomeTransactions.stream().mapToDouble(Transaction::getAmount).sum();
        double totalExpenses = expenseTransactions.stream().mapToDouble(Transaction::getAmount).sum();

        logger.info("Net Savings for user {}: {}", userId, (totalIncome - totalExpenses)); // Debug log
        return totalIncome - totalExpenses;
    }
}