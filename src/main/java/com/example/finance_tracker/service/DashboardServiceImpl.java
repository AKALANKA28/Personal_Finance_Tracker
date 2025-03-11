package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Dashboard;
import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Goal;
import com.example.finance_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Override
    public Dashboard getAdminDashboardSummary() {
        Dashboard summary = new Dashboard();
        summary.setTotalUsers((int) userRepository.count());
//        summary.setTotalIncome(incomeService.calculateTotalIncomeInBaseCurrency());
//        summary.setTotalExpenses(expenseService.calculateTotalExpensesInBaseCurrency());
        return summary;
    }

    @Override
    public Dashboard getUserDashboardSummary(String userId) {
        Dashboard summary = new Dashboard();
//        summary.setRecentTransactions(transactionRepository.findRecentTransactionsByUser(userId, 5));
        summary.setBudgets(budgetRepository.findByUserId(userId));
        summary.setGoals(goalRepository.findByUserId(userId));
//        summary.setNetSavings(transactionRepository.calculateNetSavings(userId));
        return summary;
    }
}