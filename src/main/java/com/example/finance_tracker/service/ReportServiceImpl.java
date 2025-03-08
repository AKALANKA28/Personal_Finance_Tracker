package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final CurrencyConverter currencyConverter;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final GoalsAndSavingsService goalsAndSavingsService;
    private final CurrencyUtil currencyUtil;

    @Autowired
    public ReportServiceImpl(BudgetRepository budgetRepository, ExpenseRepository expenseRepository,
                            CurrencyConverter currencyConverter, ExpenseService expenseService, IncomeService incomeService,
                             GoalsAndSavingsService goalsAndSavingsService, CurrencyUtil currencyUtil) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.currencyConverter = currencyConverter;
        this.expenseService = expenseService;
        this.incomeService = incomeService;
        this.goalsAndSavingsService = goalsAndSavingsService;
        this.currencyUtil = currencyUtil;
    }

    @Override
    public Map<String, Object> generateSpendingTrendReport(String userId, LocalDate startDate, LocalDate endDate) {
        // Fetch budgets
        List<Budget> budgets = budgetRepository.findByUserId(userId);

        // Calculate total income, expenses, and net savings (converted to base currency)
        Map<String, Double> incomeExpenseData = calculateIncomeExpenseAndNetSavings(userId, startDate, endDate);
        double totalIncome = incomeExpenseData.get("totalIncome");
        double totalExpenses = incomeExpenseData.get("totalExpenses");
        double netSavings = incomeExpenseData.get("netSavings");

        // Calculate spending trends per category (converted to base currency)
        Map<String, Map<String, Object>> spendingTrends = calculateSpendingTrends(userId, budgets, expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate), startDate, endDate);

        // Prepare the report
        return createReport(userId, startDate, endDate, totalIncome, totalExpenses, netSavings, spendingTrends);
    }

    @Override
    public Map<String, Object> generateIncomeVsExpenseReport(String userId, LocalDate startDate, LocalDate endDate) {
        // Calculate total income, expenses, and net savings (converted to base currency)
        Map<String, Double> incomeExpenseData = calculateIncomeExpenseAndNetSavings(userId, startDate, endDate);
        double totalIncome = incomeExpenseData.get("totalIncome");
        double totalExpenses = incomeExpenseData.get("totalExpenses");
        double netSavings = incomeExpenseData.get("netSavings");

        // Prepare the report
        return createReport(userId, startDate, endDate, totalIncome, totalExpenses, netSavings, null);
    }

    @Override
    public Map<String, Object> generateCategoryWiseReport(String userId, String category, LocalDate startDate, LocalDate endDate) {
        // Fetch expenses for the specified category
        List<Expense> expenses = expenseRepository.findByUserIdAndCategoryAndDateBetween(userId, category, startDate, endDate);

        // Calculate total spending for the category (converted to base currency)
        double totalSpending = expenseService.calculateTotalExpensesInBaseCurrency(userId); // Reuse existing method

        // Prepare the report
        Map<String, Object> report = new HashMap<>();
        report.put("userId", userId);
        report.put("category", category);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalSpending", totalSpending);

        return report;
    }

    // Helper Methods

    Map<String, Double> calculateIncomeExpenseAndNetSavings(String userId, LocalDate startDate, LocalDate endDate) {
        // Reuse existing methods to calculate totals
        double totalIncome = incomeService.calculateTotalIncomeInBaseCurrency(userId);
        double totalExpenses = expenseService.calculateTotalExpensesInBaseCurrency(userId);
        double netSavings = goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate);

        // Return the results in a map
        Map<String, Double> result = new HashMap<>();
        result.put("totalIncome", totalIncome);
        result.put("totalExpenses", totalExpenses);
        result.put("netSavings", netSavings);
        return result;
    }

    Map<String, Map<String, Object>> calculateSpendingTrends(String userId, List<Budget> budgets, List<Expense> expenses, LocalDate startDate, LocalDate endDate) {
        Map<String, Map<String, Object>> spendingTrends = new HashMap<>();

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);

        for (Budget budget : budgets) {
            String category = budget.getCategory();
            double totalSpending = expenses.stream()
                    .filter(expense -> expense.getCategory().equals(category))
                    .mapToDouble(expense -> currencyConverter.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount(),  baseCurrency))
                    .sum();

            long numberOfMonths = startDate.until(endDate).toTotalMonths() + 1; // Include the start month
            double averageSpending = totalSpending / numberOfMonths;

            // Compare spending against budget
            double budgetLimit = budget.getLimit();
            String budgetStatus = totalSpending > budgetLimit ? "Exceeded" : "Within Budget";

            // Add spending trend data for the category
            spendingTrends.put(category, createCategoryData(totalSpending, averageSpending, budgetLimit, budgetStatus));
        }

        return spendingTrends;
    }

    private Map<String, Object> createCategoryData(double totalSpending, double averageSpending, double budgetLimit, String budgetStatus) {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("totalSpending", totalSpending);
        categoryData.put("averageSpending", averageSpending);
        categoryData.put("budgetLimit", budgetLimit);
        categoryData.put("budgetStatus", budgetStatus);
        return categoryData;
    }

    private Map<String, Object> createReport(String userId, LocalDate startDate, LocalDate endDate,
                                             double totalIncome, double totalExpenses, double netSavings,
                                             Map<String, Map<String, Object>> spendingTrends) {
        Map<String, Object> report = new HashMap<>();
        report.put("userId", userId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalIncome", totalIncome);
        report.put("totalExpenses", totalExpenses);
        report.put("netSavings", netSavings);

        if (spendingTrends != null) {
            report.put("spendingTrends", spendingTrends);
        }

        return report;
    }
}