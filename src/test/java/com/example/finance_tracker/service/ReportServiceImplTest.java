package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CurrencyConverter currencyConverter;

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
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

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
        when(currencyConverter.convertToBaseCurrency("USD", 600.0, "USD")).thenReturn(600.0);

        // Act
        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report);
        assertEquals(10000.0, report.get("totalIncome"));
        assertEquals(8000.0, report.get("totalExpenses"));
        assertEquals(2000.0, report.get("netSavings"));

        // Verify spending trends
        Map<String, Map<String, Object>> spendingTrends = (Map<String, Map<String, Object>>) report.get("spendingTrends");
        assertNotNull(spendingTrends);
        assertEquals(1, spendingTrends.size());

        Map<String, Object> foodTrend = spendingTrends.get("Food");
        assertNotNull(foodTrend);
        assertEquals(600.0, foodTrend.get("totalSpending"));
        assertEquals(50.0, foodTrend.get("averageSpending")); // 600 / 12 months
        assertEquals(500.0, foodTrend.get("budgetLimit"));
        assertEquals("Exceeded", foodTrend.get("budgetStatus"));
    }

    @Test
    void generateIncomeVsExpenseReport_Success() {
        // Arrange
        String userId = "user123";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        // Mock income, expenses, and net savings
        when(incomeService.calculateTotalIncomeInBaseCurrency(userId)).thenReturn(10000.0);
        when(expenseService.calculateTotalExpensesInBaseCurrency(userId)).thenReturn(8000.0);
        when(goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate)).thenReturn(2000.0);

        // Act
        Map<String, Object> report = reportService.generateIncomeVsExpenseReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report);
        assertEquals(10000.0, report.get("totalIncome"));
        assertEquals(8000.0, report.get("totalExpenses"));
        assertEquals(2000.0, report.get("netSavings"));
    }



    @Test
    void generateCategoryWiseReport_Success() {
        // Arrange
        String userId = "user123";
        String category = "Food";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

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
        assertNotNull(report);
        assertEquals(userId, report.get("userId"));
        assertEquals(category, report.get("category"));
        assertEquals(startDate, report.get("startDate"));
        assertEquals(endDate, report.get("endDate"));
        assertEquals(600.0, report.get("totalSpending"));
    }



    @Test
    void calculateIncomeExpenseAndNetSavings_Success() {
        // Arrange
        String userId = "user123";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        // Mock income, expenses, and net savings
        when(incomeService.calculateTotalIncomeInBaseCurrency(userId)).thenReturn(10000.0);
        when(expenseService.calculateTotalExpensesInBaseCurrency(userId)).thenReturn(8000.0);
        when(goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate)).thenReturn(2000.0);

        // Act
        Map<String, Double> result = reportService.calculateIncomeExpenseAndNetSavings(userId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(10000.0, result.get("totalIncome"));
        assertEquals(8000.0, result.get("totalExpenses"));
        assertEquals(2000.0, result.get("netSavings"));
    }



    @Test
    void calculateSpendingTrends_Success() {
        // Arrange
        String userId = "user123";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

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
        when(currencyConverter.convertToBaseCurrency("USD", 600.0, "USD")).thenReturn(600.0);

        // Act
        Map<String, Map<String, Object>> spendingTrends = reportService.calculateSpendingTrends(userId, Collections.singletonList(budget), Collections.singletonList(expense), startDate, endDate);

        // Assert
        assertNotNull(spendingTrends);
        assertEquals(1, spendingTrends.size());

        Map<String, Object> foodTrend = spendingTrends.get("Food");
        assertNotNull(foodTrend);
        assertEquals(600.0, foodTrend.get("totalSpending"));
        assertEquals(50.0, foodTrend.get("averageSpending")); // 600 / 12 months
        assertEquals(500.0, foodTrend.get("budgetLimit"));
        assertEquals("Exceeded", foodTrend.get("budgetStatus"));
    }





}