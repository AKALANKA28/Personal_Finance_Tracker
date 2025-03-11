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
    private TransactionService transactionService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Override
    public Dashboard getAdminDashboardSummary() {
        Dashboard summary = new Dashboard();
        long totalUsers = userRepository.count();
        logger.info("Total Users: {}", totalUsers);
        summary.setTotalUsers((int) totalUsers);

        double totalIncome = transactionService.calculateTotalIncome();
        logger.info("Total Income: {}", totalIncome);
        summary.setTotalIncome(totalIncome);

        double totalExpenses = transactionService.calculateTotalExpenses();
        logger.info("Total Expenses: {}", totalExpenses);
        summary.setTotalExpenses(totalExpenses);

        return summary;
    }

    @Override
    public Dashboard getUserDashboardSummary(String userId) {
        Dashboard summary = new Dashboard();
        summary.setRecentTransactions(transactionRepository.findRecentTransactionsByUser(userId, 5));
        summary.setBudgets(budgetRepository.findByUserId(userId));
        summary.setGoals(goalRepository.findByUserId(userId));
        summary.setNetSavings(transactionService.calculateNetSavings(userId));
        return summary;
    }


}