package com.example.finance_tracker.unit;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.service.*;
import com.example.finance_tracker.util.CurrencyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CurrencyConverterImpl currencyConverterImpl;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private IncomeService incomeService;

    @Mock
    private GoalsAndSavingsService goalsAndSavingsService;

    @Mock
    private CurrencyUtil currencyUtil;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateSpendingTrendReport_Success() {
        // Arrange
        String userId = "user123";

        // Create start and end dates using java.util.Date
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 1); // January 1, 2023
        Date startDate = calendar.getTime();

        calendar.set(2023, Calendar.DECEMBER, 31); // December 31, 2023
        Date endDate = calendar.getTime();

        // Mock budgets
        Budget budget = new Budget();
        budget.setCategory("Food");
        budget.setLimit(500.0);
        when(budgetRepository.findByUserId(userId)).thenReturn(Collections.singletonList(budget));

        // Mock income, expenses, and net savings
        when(incomeService.calculateTotalIncomeInBaseCurrency(userId)).thenReturn(10000.0);
        when(expenseService.calculateTotalExpensesInBaseCurrency(userId)).thenReturn(8000.0);
        when(goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate)).thenReturn(2000.0);

        // Mock expenses
        Expense expense = new Expense();
        expense.setCategory("Food");
        expense.setAmount(600.0);
        expense.setCurrencyCode("USD");
        when(expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate))
                .thenReturn(Collections.singletonList(expense));

        // Mock currency conversion
        when(currencyUtil.getBaseCurrencyForUser(userId)).thenReturn("USD");
        when(currencyConverterImpl.convertToBaseCurrency("USD", 600.0, "USD")).thenReturn(600.0);

        // Act
        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report, "Report should not be null");
        assertEquals(10000.0, report.get("totalIncome"), "Total income should match");
        assertEquals(8000.0, report.get("totalExpenses"), "Total expenses should match");
        assertEquals(2000.0, report.get("netSavings"), "Net savings should match");

        // Verify spending trends
        Map<String, Map<String, Object>> spendingTrends = (Map<String, Map<String, Object>>) report.get("spendingTrends");
        assertNotNull(spendingTrends, "Spending trends should not be null");
        assertEquals(1, spendingTrends.size(), "Spending trends should contain exactly one category");

        Map<String, Object> foodTrend = spendingTrends.get("Food");
        assertNotNull(foodTrend, "Food trend should not be null");
        assertEquals(600.0, foodTrend.get("totalSpending"), "Total spending for Food should match");
        assertEquals(50.0, foodTrend.get("averageSpending"), "Average spending for Food should match"); // 600 / 12 months
        assertEquals(500.0, foodTrend.get("budgetLimit"), "Budget limit for Food should match");
        assertEquals("Exceeded", foodTrend.get("budgetStatus"), "Budget status for Food should be 'Exceeded'");
    }

    @Test
    void generateIncomeVsExpenseReport_Success() {
        // Arrange
        String userId = "user123";

        // Create start and end dates using java.util.Date
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 1); // January 1, 2023
        Date startDate = calendar.getTime();

        calendar.set(2023, Calendar.DECEMBER, 31); // December 31, 2023
        Date endDate = calendar.getTime();

        // Mock income, expenses, and net savings
        when(incomeService.calculateTotalIncomeInBaseCurrency(userId)).thenReturn(10000.0);
        when(expenseService.calculateTotalExpensesInBaseCurrency(userId)).thenReturn(8000.0);
        when(goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate)).thenReturn(2000.0);

        // Act
        Map<String, Object> report = reportService.generateIncomeVsExpenseReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report, "Report should not be null");
        assertEquals(10000.0, report.get("totalIncome"), "Total income should match");
        assertEquals(8000.0, report.get("totalExpenses"), "Total expenses should match");
        assertEquals(2000.0, report.get("netSavings"), "Net savings should match");
    }



    @Test
    void generateCategoryWiseReport_Success() {
        // Arrange
        String userId = "user123";
        String category = "Food";

        // Create start and end dates using java.util.Date
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 1); // January 1, 2023
        Date startDate = calendar.getTime();

        calendar.set(2023, Calendar.DECEMBER, 31); // December 31, 2023
        Date endDate = calendar.getTime();

        // Mock expenses
        Expense expense = new Expense();
        expense.setCategory("Food");
        expense.setAmount(600.0);
        expense.setCurrencyCode("USD");
        when(expenseRepository.findByUserIdAndCategoryAndDateBetween(userId, category, startDate, endDate))
                .thenReturn(Collections.singletonList(expense));

        // Mock total spending calculation
        when(expenseService.calculateTotalExpensesInBaseCurrency(userId)).thenReturn(600.0);

        // Act
        Map<String, Object> report = reportService.generateCategoryWiseReport(userId, category, startDate, endDate);

        // Assert
        assertNotNull(report, "Report should not be null");
        assertEquals(userId, report.get("userId"), "User ID should match");
        assertEquals(category, report.get("category"), "Category should match");
        assertEquals(startDate, report.get("startDate"), "Start date should match");
        assertEquals(endDate, report.get("endDate"), "End date should match");
        assertEquals(600.0, report.get("totalSpending"), "Total spending should match");
    }


//    @Test
//    void calculateIncomeExpenseAndNetSavings_Success() {
//        // Arrange
//        String userId = "user123";
//
//        // Create start and end dates using java.util.Date
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(2023, Calendar.JANUARY, 1); // January 1, 2023
//        Date startDate = calendar.getTime();
//
//        calendar.set(2023, Calendar.DECEMBER, 31); // December 31, 2023
//        Date endDate = calendar.getTime();
//
//        // Mock income, expenses, and net savings
//        when(incomeService.calculateTotalIncomeInBaseCurrency(userId)).thenReturn(10000.0);
//        when(expenseService.calculateTotalExpensesInBaseCurrency(userId)).thenReturn(8000.0);
//        when(goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate)).thenReturn(2000.0);
//
//        // Act
//        Map<String, Double> result = reportService.calculateIncomeExpenseAndNetSavings(userId, startDate, endDate);
//
//        // Assert
//        assertNotNull(result, "Result should not be null");
//        assertEquals(3, result.size(), "Result map should contain exactly 3 entries");
//        assertEquals(10000.0, result.get("totalIncome"), 0.001, "Total income should match");
//        assertEquals(8000.0, result.get("totalExpenses"), 0.001, "Total expenses should match");
//        assertEquals(2000.0, result.get("netSavings"), 0.001, "Net savings should match");
//    }
//


    @Test
    void calculateSpendingTrends_Success() {
        // Arrange
        String userId = "user123";

        // Create start and end dates using java.util.Date
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 1); // January 1, 2023
        Date startDate = calendar.getTime();

        calendar.set(2023, Calendar.DECEMBER, 31); // December 31, 2023
        Date endDate = calendar.getTime();

        // Mock budgets
        Budget budget = new Budget();
        budget.setCategory("Food");
        budget.setLimit(500.0);
        when(budgetRepository.findByUserId(userId)).thenReturn(Collections.singletonList(budget));

        // Mock expenses
        Expense expense = new Expense();
        expense.setCategory("Food");
        expense.setAmount(600.0);
        expense.setCurrencyCode("USD");
        when(expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate))
                .thenReturn(Collections.singletonList(expense));

        // Mock currency conversion
        when(currencyUtil.getBaseCurrencyForUser(userId)).thenReturn("USD");
        when(currencyConverterImpl.convertToBaseCurrency("USD", 600.0, "USD")).thenReturn(600.0);

        // Act
        Map<String, Map<String, Object>> spendingTrends = reportService.calculateSpendingTrends(
                userId, Collections.singletonList(budget), Collections.singletonList(expense), startDate, endDate);

        // Assert
        assertNotNull(spendingTrends, "Spending trends should not be null");
        assertEquals(1, spendingTrends.size(), "Spending trends should contain exactly one category");

        Map<String, Object> foodTrend = spendingTrends.get("Food");
        assertNotNull(foodTrend, "Food trend should not be null");
        assertEquals(600.0, foodTrend.get("totalSpending"), "Total spending for Food should match");
        assertEquals(50.0, foodTrend.get("averageSpending"), "Average spending for Food should match"); // 600 / 12 months
        assertEquals(500.0, foodTrend.get("budgetLimit"), "Budget limit for Food should match");
        assertEquals("Exceeded", foodTrend.get("budgetStatus"), "Budget status for Food should be 'Exceeded'");
    }

}