package com.example.finance_tracker.integration;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.service.*;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReportServiceImplIntegrationTest {

    @Autowired
    private ReportService reportService;

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

    private SimpleDateFormat dateFormat;
    private Date startDate;
    private Date endDate;
    private String userId;
    private List<Budget> mockBudgets;
    private List<Expense> mockExpenses;

    @BeforeEach
    public void setup() throws ParseException {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startDate = dateFormat.parse("2023-01-01");
        endDate = dateFormat.parse("2023-03-31");
        userId = "user123";

        // Setup mock budgets
        mockBudgets = new ArrayList<>();
        mockBudgets.add(createBudget("groceries", 500.0));
        mockBudgets.add(createBudget("entertainment", 300.0));
        mockBudgets.add(createBudget("transportation", 200.0));

        // Setup mock expenses
        mockExpenses = new ArrayList<>();
        mockExpenses.add(createExpense("groceries", 150.0, "2023-01-15", "USD"));
        mockExpenses.add(createExpense("groceries", 180.0, "2023-02-15", "USD"));
        mockExpenses.add(createExpense("groceries", 200.0, "2023-03-15", "USD"));
        mockExpenses.add(createExpense("entertainment", 100.0, "2023-01-20", "USD"));
        mockExpenses.add(createExpense("entertainment", 120.0, "2023-02-20", "USD"));
        mockExpenses.add(createExpense("transportation", 50.0, "2023-01-25", "USD"));
        mockExpenses.add(createExpense("transportation", 60.0, "2023-03-25", "USD"));

        reportService = new ReportServiceImpl(
                budgetRepository,
                expenseRepository,
                currencyConverter,
                expenseService,
                incomeService,
                goalsAndSavingsService,
                currencyUtil
        );

        // Configure mocks
        setupMocks();
    }

    private void setupMocks() {
        when(budgetRepository.findByUserId(userId)).thenReturn(mockBudgets);
        when(expenseRepository.findByUserIdAndDateBetween(eq(userId), any(Date.class), any(Date.class)))
                .thenReturn(mockExpenses);

        when(currencyUtil.getBaseCurrencyForUser(userId)).thenReturn("USD");

        when(incomeService.calculateTotalIncomeInBaseCurrency(userId)).thenReturn(3000.0);
        when(expenseService.calculateTotalExpensesInBaseCurrency(userId)).thenReturn(860.0);
        when(goalsAndSavingsService.calculateNetSavings(eq(userId), any(Date.class), any(Date.class)))
                .thenReturn(2140.0);

        // Configure currency conversion to return the same amount (as we're using USD)
        when(currencyConverter.convertToBaseCurrency(eq("USD"), anyDouble(), eq("USD")))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    private Budget createBudget(String category, double limit) {
        Budget budget = new Budget();
        budget.setCategory(category);
        budget.setLimit(limit);
        budget.setUserId(userId);
        return budget;
    }

    private Expense createExpense(String category, double amount, String date, String currencyCode) throws ParseException {
        Expense expense = new Expense();
        expense.setCategory(category);
        expense.setAmount(amount);
        expense.setDate(dateFormat.parse(date));
        expense.setUserId(userId);
        expense.setCurrencyCode(currencyCode);
        return expense;
    }

    @Test
    public void testGenerateSpendingTrendReport_Success() {
        // Act
        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report);
        assertEquals(userId, report.get("userId"));
        assertEquals(startDate, report.get("startDate"));
        assertEquals(endDate, report.get("endDate"));
        assertEquals(3000.0, report.get("totalIncome"));
        assertEquals(860.0, report.get("totalExpenses"));
        assertEquals(2140.0, report.get("netSavings"));

        // Verify spending trends
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> spendingTrends = (Map<String, Map<String, Object>>) report.get("spendingTrends");
        assertNotNull(spendingTrends);
        assertTrue(spendingTrends.containsKey("groceries"));
        assertTrue(spendingTrends.containsKey("entertainment"));
        assertTrue(spendingTrends.containsKey("transportation"));
    }

    @Test
    public void testGenerateIncomeVsExpenseReport_Success() {
        // Act
        Map<String, Object> report = reportService.generateIncomeVsExpenseReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report);
        assertEquals(userId, report.get("userId"));
        assertEquals(startDate, report.get("startDate"));
        assertEquals(endDate, report.get("endDate"));
        assertEquals(3000.0, report.get("totalIncome"));
        assertEquals(860.0, report.get("totalExpenses"));
        assertEquals(2140.0, report.get("netSavings"));

        // Verify spending trends is not included
        assertNull(report.get("spendingTrends"));
    }

    @Test
    public void testGenerateCategoryWiseReport_Success() {
        // Arrange
        String category = "groceries";

        // Act
        Map<String, Object> report = reportService.generateCategoryWiseReport(userId, category, startDate, endDate);

        // Assert
        assertNotNull(report);
        assertEquals(userId, report.get("userId"));
        assertEquals(category, report.get("category"));
        assertEquals(startDate, report.get("startDate"));
        assertEquals(endDate, report.get("endDate"));
        assertEquals(860.0, report.get("totalSpending")); // This uses the mocked expenseService
    }

    @Test
    public void testGenerateSpendingTrendReport_WithNullUserId_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                reportService.generateSpendingTrendReport(null, startDate, endDate)
        );
    }

    @Test
    public void testGenerateSpendingTrendReport_WithNullStartDate_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                reportService.generateSpendingTrendReport(userId, null, endDate)
        );
    }

    @Test
    public void testGenerateSpendingTrendReport_WithNullEndDate_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                reportService.generateSpendingTrendReport(userId, startDate, null)
        );
    }

    @Test
    public void testGenerateSpendingTrendReport_WithStartDateAfterEndDate_ThrowsException() throws ParseException {
        // Arrange
        Date invalidStartDate = dateFormat.parse("2023-04-01");

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                reportService.generateSpendingTrendReport(userId, invalidStartDate, endDate)
        );
    }

    @Test
    public void testGenerateSpendingTrendReport_WithNoBudgets() {
        // Arrange
        when(budgetRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> spendingTrends = (Map<String, Map<String, Object>>) report.get("spendingTrends");
        assertNotNull(spendingTrends);
        assertTrue(spendingTrends.isEmpty());
    }

    @Test
    public void testGenerateSpendingTrendReport_WithNoExpenses() {
        // Arrange
        when(expenseRepository.findByUserIdAndDateBetween(eq(userId), any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> spendingTrends = (Map<String, Map<String, Object>>) report.get("spendingTrends");
        assertNotNull(spendingTrends);
        assertTrue(spendingTrends.isEmpty());
    }

    @Test
    public void testGenerateSpendingTrendReport_WithMultipleCurrencies() throws ParseException {
        // Arrange
        List<Expense> multiCurrencyExpenses = new ArrayList<>(mockExpenses);
        multiCurrencyExpenses.add(createExpense("groceries", 100.0, "2023-02-01", "EUR"));

        when(expenseRepository.findByUserIdAndDateBetween(eq(userId), any(Date.class), any(Date.class)))
                .thenReturn(multiCurrencyExpenses);

        // Mock EUR to USD conversion (1 EUR = 1.1 USD)
        when(currencyConverter.convertToBaseCurrency(eq("EUR"), anyDouble(), eq("USD")))
                .thenAnswer(invocation -> (Double)invocation.getArgument(1) * 1.1);

        // Act
        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, startDate, endDate);

        // Assert
        assertNotNull(report);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> spendingTrends = (Map<String, Map<String, Object>>) report.get("spendingTrends");
        assertNotNull(spendingTrends);
        assertTrue(spendingTrends.containsKey("groceries"));
    }

    @Test
    public void testGenerateCategoryWiseReport_WithInvalidCategory() {
        // Arrange
        String invalidCategory = null;

        // Act & Assert
        assertThrows(InvalidInputException.class, () ->
                reportService.generateCategoryWiseReport(userId, invalidCategory, startDate, endDate)
        );
    }

    @Test
    public void testGenerateReportWithLongDateRange() throws ParseException {
        // Arrange
        Date longRangeStartDate = dateFormat.parse("2022-01-01");
        Date longRangeEndDate = dateFormat.parse("2023-12-31");

        // Act
        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, longRangeStartDate, longRangeEndDate);

        // Assert
        assertNotNull(report);
        assertEquals(longRangeStartDate, report.get("startDate"));
        assertEquals(longRangeEndDate, report.get("endDate"));
    }
}