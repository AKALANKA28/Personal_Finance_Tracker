package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.exception.InvalidInputException;
import com.example.finance_tracker.exception.CurrencyConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final CurrencyConverterImpl currencyConverterImpl;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final GoalsAndSavingsService goalsAndSavingsService;
    private final CurrencyUtil currencyUtil;

    @Autowired
    public ReportServiceImpl(BudgetRepository budgetRepository, ExpenseRepository expenseRepository,
                             CurrencyConverterImpl currencyConverterImpl, ExpenseService expenseService, IncomeService incomeService,
                             GoalsAndSavingsService goalsAndSavingsService, CurrencyUtil currencyUtil) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.currencyConverterImpl = currencyConverterImpl;
        this.expenseService = expenseService;
        this.incomeService = incomeService;
        this.goalsAndSavingsService = goalsAndSavingsService;
        this.currencyUtil = currencyUtil;
    }

    @Override
    public Map<String, Object> generateSpendingTrendReport(String userId, Date startDate, Date endDate) {
        logger.info("Generating spending trend report for user: {}, from {} to {}", userId, startDate, endDate);

        // Validate input
        if (userId == null || startDate == null || endDate == null) {
            logger.error("Invalid input: userId, startDate, or endDate cannot be null");
            throw new InvalidInputException("Invalid input: userId, startDate, or endDate cannot be null");
        }

        // Fetch budgets
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        if (budgets.isEmpty()) {
            logger.warn("No budgets found for user: {}", userId);
        }
        logger.debug("Fetched {} budgets for user: {}", budgets.size(), userId);

        // Calculate total income, expenses, and net savings (converted to base currency)
        Map<String, Double> incomeExpenseData = calculateIncomeExpenseAndNetSavings(userId, startDate, endDate);
        double totalIncome = incomeExpenseData.get("totalIncome");
        double totalExpenses = incomeExpenseData.get("totalExpenses");
        double netSavings = incomeExpenseData.get("netSavings");

        logger.debug("Total income: {}, Total expenses: {}, Net savings: {}", totalIncome, totalExpenses, netSavings);

        // Calculate spending trends per category (converted to base currency)
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        if (expenses.isEmpty()) {
            logger.warn("No expenses found for user: {} between {} and {}", userId, startDate, endDate);
        }
        Map<String, Map<String, Object>> spendingTrends = calculateSpendingTrends(userId, budgets, expenses, startDate, endDate);
        logger.debug("Spending trends calculated for {} categories", spendingTrends.size());

        // Prepare the report
        Map<String, Object> report = createReport(userId, startDate, endDate, totalIncome, totalExpenses, netSavings, spendingTrends);
        logger.info("Spending trend report generated successfully for user: {}", userId);

        return report;
    }

    @Override
    public Map<String, Object> generateIncomeVsExpenseReport(String userId, Date startDate, Date endDate) {
        logger.info("Generating income vs expense report for user: {}, from {} to {}", userId, startDate, endDate);

        // Validate input
        if (userId == null || startDate == null || endDate == null) {
            logger.error("Invalid input: userId, startDate, or endDate cannot be null");
            throw new InvalidInputException("Invalid input: userId, startDate, or endDate cannot be null");
        }

        // Calculate total income, expenses, and net savings (converted to base currency)
        Map<String, Double> incomeExpenseData = calculateIncomeExpenseAndNetSavings(userId, startDate, endDate);
        double totalIncome = incomeExpenseData.get("totalIncome");
        double totalExpenses = incomeExpenseData.get("totalExpenses");
        double netSavings = incomeExpenseData.get("netSavings");

        logger.debug("Total income: {}, Total expenses: {}, Net savings: {}", totalIncome, totalExpenses, netSavings);

        // Prepare the report
        Map<String, Object> report = createReport(userId, startDate, endDate, totalIncome, totalExpenses, netSavings, null);
        logger.info("Income vs expense report generated successfully for user: {}", userId);

        return report;
    }

    @Override
    public Map<String, Object> generateCategoryWiseReport(String userId, String category, Date startDate, Date endDate) {
        logger.info("Generating category-wise report for user: {}, category: {}, from {} to {}", userId, category, startDate, endDate);

        // Validate input
        if (userId == null || category == null || startDate == null || endDate == null) {
            logger.error("Invalid input: userId, category, startDate, or endDate cannot be null");
            throw new InvalidInputException("Invalid input: userId, category, startDate, or endDate cannot be null");
        }

        // Calculate total spending for the category (converted to base currency)
        double totalSpending = expenseService.calculateTotalExpensesInBaseCurrency(userId); // Reuse existing method
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

    private Map<String, Double> calculateIncomeExpenseAndNetSavings(String userId, Date startDate, Date endDate) {
        logger.debug("Calculating income, expenses, and net savings for user: {}", userId);

        // Reuse existing methods to calculate totals
        double totalIncome = incomeService.calculateTotalIncomeInBaseCurrency(userId);
        double totalExpenses = expenseService.calculateTotalExpensesInBaseCurrency(userId);
        double netSavings = goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate);

        logger.debug("Calculated totals - Income: {}, Expenses: {}, Net Savings: {}", totalIncome, totalExpenses, netSavings);

        // Return the results in a map
        Map<String, Double> result = new HashMap<>();
        result.put("totalIncome", totalIncome);
        result.put("totalExpenses", totalExpenses);
        result.put("netSavings", netSavings);
        return result;
    }

    public Map<String, Map<String, Object>> calculateSpendingTrends(String userId, List<Budget> budgets, List<Expense> expenses, Date startDate, Date endDate) {
        logger.debug("Calculating spending trends for user: {}", userId);

        Map<String, Map<String, Object>> spendingTrends = new HashMap<>();

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);
        logger.debug("Base currency for user {}: {}", userId, baseCurrency);

        // Calculate the number of months between startDate and endDate
        long numberOfMonths = getNumberOfMonths(startDate, endDate) + 1; // Include the start month
        logger.debug("Number of months between {} and {}: {}", startDate, endDate, numberOfMonths);

        for (Budget budget : budgets) {
            String category = budget.getCategory();
            logger.debug("Processing category: {}", category);

            // Filter expenses by category and date range
            List<Expense> filteredExpenses = expenses.stream()
                    .filter(expense -> {
                        boolean isCategoryMatch = expense.getCategory().trim().equalsIgnoreCase(category.trim());
                        boolean isDateInRange = !expense.getDate().before(startDate) && !expense.getDate().after(endDate);
                        return isCategoryMatch && isDateInRange;
                    })
                    .toList();

            logger.debug("Found {} expenses for category: {}", filteredExpenses.size(), category);

            // Calculate total spending for the category (converted to base currency)
            double totalSpending = filteredExpenses.stream()
                    .mapToDouble(expense -> {
                        try {
                            return currencyConverterImpl.convertToBaseCurrency(
                                    expense.getCurrencyCode(), expense.getAmount(), baseCurrency);
                        } catch (Exception e) {
                            logger.error("Failed to convert currency for expense: {}", expense, e);
                            throw new CurrencyConversionException("Failed to convert currency", e);
                        }
                    })
                    .sum();

            logger.debug("Total spending for category {}: {}", category, totalSpending);

            // Calculate average spending
            double averageSpending = totalSpending / numberOfMonths;
            logger.debug("Average spending for category {}: {}", category, averageSpending);

            // Compare spending against budget
            double budgetLimit = budget.getLimit();
            String budgetStatus = totalSpending > budgetLimit ? "Exceeded" : "Within Budget";
            logger.debug("Budget status for category {}: {}", category, budgetStatus);

            // Add spending trend data for the category
            spendingTrends.put(category, createCategoryData(totalSpending, averageSpending, budgetLimit, budgetStatus));
        }

        logger.debug("Spending trends calculated for {} categories", spendingTrends.size());
        return spendingTrends;
    }

    // Helper method to calculate the number of months between two Date objects
    private long getNumberOfMonths(Date startDate, Date endDate) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        int startYear = startCal.get(Calendar.YEAR);
        int startMonth = startCal.get(Calendar.MONTH);

        int endYear = endCal.get(Calendar.YEAR);
        int endMonth = endCal.get(Calendar.MONTH);

        // Calculate the total number of months
        return (endYear - startYear) * 12L + (endMonth - startMonth);
    }

    private Map<String, Object> createCategoryData(double totalSpending, double averageSpending, double budgetLimit, String budgetStatus) {
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("totalSpending", totalSpending);
        categoryData.put("averageSpending", averageSpending);
        categoryData.put("budgetLimit", budgetLimit);
        categoryData.put("budgetStatus", budgetStatus);
        return categoryData;
    }

    private Map<String, Object> createReport(String userId, Date startDate, Date endDate,
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