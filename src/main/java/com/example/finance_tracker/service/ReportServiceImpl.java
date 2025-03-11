package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.InvalidInputException;
import com.example.finance_tracker.exception.CurrencyConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final CurrencyConverter currencyConverter;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final GoalsAndSavingsService goalsAndSavingsService;
    private final CurrencyUtil currencyUtil;

    @Autowired
    public ReportServiceImpl(BudgetRepository budgetRepository,
                             ExpenseRepository expenseRepository,
                             CurrencyConverter currencyConverter,
                             ExpenseService expenseService,
                             IncomeService incomeService,
                             GoalsAndSavingsService goalsAndSavingsService,
                             CurrencyUtil currencyUtil) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.currencyConverter = currencyConverter;
        this.expenseService = expenseService;
        this.incomeService = incomeService;
        this.goalsAndSavingsService = goalsAndSavingsService;
        this.currencyUtil = currencyUtil;
    }

    @Override
    public Map<String, Object> generateSpendingTrendReport(String userId, Date startDate, Date endDate) {
        logger.info("Generating spending trend report for user: {}, from {} to {}", userId, startDate, endDate);
        validateInput(userId, startDate, endDate);

        // Fetch budgets
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        logBudgetInfo(userId, budgets);

        // Calculate income, expenses, and savings
        FinancialSummary financialSummary = calculateFinancialSummary(userId, startDate, endDate);

        // Calculate spending trends per category
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        logExpensesInfo(userId, startDate, endDate, expenses);

        Map<String, Map<String, Object>> spendingTrends = calculateSpendingTrends(userId, budgets, expenses, startDate, endDate);

        // Prepare the report
        Map<String, Object> report = createReport(userId, startDate, endDate, financialSummary, spendingTrends);
        logger.info("Spending trend report generated successfully for user: {}", userId);

        return report;
    }

    @Override
    public Map<String, Object> generateIncomeVsExpenseReport(String userId, Date startDate, Date endDate) {
        logger.info("Generating income vs expense report for user: {}, from {} to {}", userId, startDate, endDate);
        validateInput(userId, startDate, endDate);

        // Calculate income, expenses, and savings
        FinancialSummary financialSummary = calculateFinancialSummary(userId, startDate, endDate);

        // Prepare the report without spending trends
        Map<String, Object> report = createReport(userId, startDate, endDate, financialSummary, null);
        logger.info("Income vs expense report generated successfully for user: {}", userId);

        return report;
    }

    @Override
    public Map<String, Object> generateCategoryWiseReport(String userId, String category, Date startDate, Date endDate) {
        logger.info("Generating category-wise report for user: {}, category: {}, from {} to {}",
                userId, category, startDate, endDate);

        validateCategoryInput(userId, category, startDate, endDate);

        // Calculate total spending for the category
        double totalSpending = expenseService.calculateTotalExpensesInBaseCurrency(userId);
        logger.debug("Total spending for category {}: {}", category, totalSpending);

        // Prepare the report
        Map<String, Object> report = new HashMap<>();
        report.put("userId", userId);
        report.put("category", category);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalSpending", totalSpending);

        logger.info("Category-wise report generated successfully for user: {}, category: {}", userId, category);

        return report;
    }






    // Helper Methods
    private void validateInput(String userId, Date startDate, Date endDate) {
        if (userId == null || startDate == null || endDate == null) {
            logger.error("Invalid input: userId, startDate, or endDate cannot be null");
            throw new InvalidInputException("Invalid input: userId, startDate, or endDate cannot be null");
        }

        if (startDate.after(endDate)) {
            logger.error("Invalid date range: startDate must be before endDate");
            throw new InvalidInputException("Invalid date range: startDate must be before endDate");
        }
    }

    private void validateCategoryInput(String userId, String category, Date startDate, Date endDate) {
        if (userId == null || category == null || startDate == null || endDate == null) {
            logger.error("Invalid input: userId, category, startDate, or endDate cannot be null");
            throw new InvalidInputException("Invalid input: userId, category, startDate, or endDate cannot be null");
        }

        if (startDate.after(endDate)) {
            logger.error("Invalid date range: startDate must be before endDate");
            throw new InvalidInputException("Invalid date range: startDate must be before endDate");
        }
    }

    private void logBudgetInfo(String userId, List<Budget> budgets) {
        if (budgets.isEmpty()) {
            logger.warn("No budgets found for user: {}", userId);
        } else {
            logger.debug("Fetched {} budgets for user: {}", budgets.size(), userId);
        }
    }

    private void logExpensesInfo(String userId, Date startDate, Date endDate, List<Expense> expenses) {
        if (expenses.isEmpty()) {
            logger.warn("No expenses found for user: {} between {} and {}", userId, startDate, endDate);
        } else {
            logger.debug("Found {} expenses for user {} between {} and {}",
                    expenses.size(), userId, startDate, endDate);
        }
    }

    private static class FinancialSummary {
        final double totalIncome;
        final double totalExpenses;
        final double netSavings;

        FinancialSummary(double totalIncome, double totalExpenses, double netSavings) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.netSavings = netSavings;
        }
    }

    private FinancialSummary calculateFinancialSummary(String userId, Date startDate, Date endDate) {
        logger.debug("Calculating income, expenses, and net savings for user: {}", userId);

        double totalIncome = incomeService.calculateTotalIncomeInBaseCurrency(userId);
        double totalExpenses = expenseService.calculateTotalExpensesInBaseCurrency(userId);
        double netSavings = goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate);

        logger.debug("Calculated totals - Income: {}, Expenses: {}, Net Savings: {}",
                totalIncome, totalExpenses, netSavings);

        return new FinancialSummary(totalIncome, totalExpenses, netSavings);
    }

    public Map<String, Map<String, Object>> calculateSpendingTrends(
            String userId, List<Budget> budgets, List<Expense> expenses, Date startDate, Date endDate) {
        logger.debug("Calculating spending trends for user: {}", userId);

        Map<String, Map<String, Object>> spendingTrends = new HashMap<>();
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);
        logger.debug("Base currency for user {}: {}", userId, baseCurrency);

        int numberOfMonths = getNumberOfMonths(startDate, endDate);
        logger.debug("Number of months between {} and {}: {}", startDate, endDate, numberOfMonths);

        // Normalize expenses by category (convert to lowercase)
        Map<String, List<Expense>> expensesByCategory = expenses.stream()
                .collect(Collectors.groupingBy(expense -> expense.getCategory().trim().toLowerCase()));

        // Log the grouped expenses
        logger.debug("Expenses grouped by category: {}", expensesByCategory);

        for (Budget budget : budgets) {
            // Normalize budget category (convert to lowercase)
            String normalizedCategory = budget.getCategory().trim().toLowerCase();
            logger.debug("Processing category: {}", normalizedCategory);

            // Get expenses for this category
            List<Expense> categoryExpenses = expensesByCategory.getOrDefault(normalizedCategory, Collections.emptyList());
            logger.debug("Found {} expenses for category: {}", categoryExpenses.size(), normalizedCategory);

            // Skip categories with no expenses
            if (categoryExpenses.isEmpty()) {
                logger.debug("No expenses found for category: {}", normalizedCategory);
                continue;
            }

            // Calculate spending data for this category
            processCategory(userId, normalizedCategory, categoryExpenses, budget, baseCurrency, numberOfMonths, spendingTrends);
        }

        logger.debug("Spending trends calculated for {} categories", spendingTrends.size());
        return spendingTrends;
    }

    private void processCategory(String userId, String normalizedCategory, List<Expense> expenses, Budget budget,
                                 String baseCurrency, int numberOfMonths, Map<String, Map<String, Object>> spendingTrends) {
        try {
            // Get the original category name from the first expense (assuming all expenses in the list have the same category)
            String originalCategory = expenses.get(0).getCategory();

            // Calculate total spending for the category
            double totalSpending = calculateTotalSpending(expenses, baseCurrency);
            logger.debug("Total spending for category {}: {}", originalCategory, totalSpending);

            // Calculate average spending
            double averageSpending = (numberOfMonths > 0) ? totalSpending / numberOfMonths : 0;
            logger.debug("Average spending for category {}: {}", originalCategory, averageSpending);

            // Compare spending against budget
            double budgetLimit = budget.getLimit();
            String budgetStatus = totalSpending > budgetLimit ? "Exceeded" : "Within Budget";
            logger.debug("Budget status for category {}: {}", originalCategory, budgetStatus);

            // Add spending trend data for the category
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("totalSpending", totalSpending);
            categoryData.put("averageSpending", averageSpending);
            categoryData.put("budgetLimit", budgetLimit);
            categoryData.put("budgetStatus", budgetStatus);

            spendingTrends.put(originalCategory, categoryData);
        } catch (Exception e) {
            logger.error("Error processing category {}: {}", normalizedCategory, e.getMessage());
            // Continue processing other categories
        }
    }
    private double calculateTotalSpending(List<Expense> expenses, String baseCurrency) {
        return expenses.stream()
                .mapToDouble(expense -> {
                    try {
                        return currencyConverter.convertToBaseCurrency(
                                expense.getCurrencyCode(), expense.getAmount(), baseCurrency);
                    } catch (Exception e) {
                        logger.error("Failed to convert currency for expense: {}", expense, e);
                        throw new CurrencyConversionException("Failed to convert currency", e);
                    }
                })
                .sum();
    }

    private int getNumberOfMonths(Date startDate, Date endDate) {
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Period period = Period.between(start, end);
        return period.getYears() * 12 + period.getMonths() + 1; // Include the start month
    }

    private Map<String, Object> createReport(String userId, Date startDate, Date endDate,
                                             FinancialSummary summary, Map<String, Map<String, Object>> spendingTrends) {
        Map<String, Object> report = new HashMap<>();
        report.put("userId", userId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalIncome", summary.totalIncome);
        report.put("totalExpenses", summary.totalExpenses);
        report.put("netSavings", summary.netSavings);

        if (spendingTrends != null) {
            report.put("spendingTrends", spendingTrends);
        }

        return report;
    }
}