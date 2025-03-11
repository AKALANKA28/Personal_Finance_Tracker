package com.example.finance_tracker.unit;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.exception.CurrencyConversionException;
import com.example.finance_tracker.service.CurrencyConverter;
import com.example.finance_tracker.service.ReportServiceImpl;
import com.example.finance_tracker.util.CurrencyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpendingTrendsCalculationTest {

    @Mock
    private CurrencyConverter currencyConverter;

    @Mock
    private CurrencyUtil currencyUtil;

    @InjectMocks
    private ReportServiceImpl reportService;

    private SimpleDateFormat dateFormat;
    private Date startDate;
    private Date endDate;
    private String userId;
    private List<Budget> budgets;
    private List<Expense> expenses;

    @BeforeEach
    public void setup() throws ParseException {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startDate = dateFormat.parse("2023-01-01");
        endDate = dateFormat.parse("2023-03-31");
        userId = "user123";

        // Setup test data
        setupTestData();

        // Configure mocks
        when(currencyUtil.getBaseCurrencyForUser(userId)).thenReturn("USD");

        // Setup currency conversion
        when(currencyConverter.convertToBaseCurrency(eq("USD"), anyDouble(), eq("USD")))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(currencyConverter.convertToBaseCurrency(eq("EUR"), anyDouble(), eq("USD")))
                .thenAnswer(invocation -> (Double)invocation.getArgument(1) * 1.1);
        when(currencyConverter.convertToBaseCurrency(eq("GBP"), anyDouble(), eq("USD")))
                .thenAnswer(invocation -> (Double)invocation.getArgument(1) * 1.3);
    }

    private void setupTestData() throws ParseException {
        // Create budgets
        budgets = new ArrayList<>();
        budgets.add(createBudget("groceries", 500.0));
        budgets.add(createBudget("entertainment", 300.0));
        budgets.add(createBudget("transportation", 200.0));
        budgets.add(createBudget("housing", 1000.0));  // No expenses for this category

        // Create expenses with different currencies
        expenses = new ArrayList<>();
        // Groceries
        expenses.add(createExpense("groceries", 150.0, "2023-01-15", "USD"));
        expenses.add(createExpense("groceries", 160.0, "2023-02-15", "EUR"));  // Will be 176.0 USD
        expenses.add(createExpense("groceries", 200.0, "2023-03-15", "USD"));
        // Entertainment
        expenses.add(createExpense("entertainment", 100.0, "2023-01-20", "USD"));
        expenses.add(createExpense("entertainment", 90.0, "2023-02-20", "GBP"));   // Will be 117.0 USD
        expenses.add(createExpense("entertainment", 150.0, "2023-03-20", "USD"));
        // Transportation
        expenses.add(createExpense("transportation", 50.0, "2023-01-25", "USD"));
        expenses.add(createExpense("transportation", 60.0, "2023-03-25", "USD"));
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
    public void testCalculateSpendingTrends_Success() {
        // Act
        Map<String, Map<String, Object>> result = reportService.calculateSpendingTrends(
                userId, budgets, expenses, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify groceries category
        assertTrue(result.containsKey("groceries"));
        Map<String, Object> groceriesData = result.get("groceries");

        assertEquals(526.0, (double) groceriesData.get("totalSpending"), 0.01);  // 150 + 176 + 200
        assertEquals(175.33, (double) groceriesData.get("averageSpending"), 0.01);  // 526 / 3
        assertEquals(500.0, (double) groceriesData.get("budgetLimit"));
        assertEquals("Exceeded", groceriesData.get("budgetStatus"));

        // Verify entertainment category
        assertTrue(result.containsKey("entertainment"));
        Map<String, Object> entertainmentData = result.get("entertainment");

        assertEquals(367.0, (double) entertainmentData.get("totalSpending"), 0.01);  // 100 + 117 + 150
        assertEquals(122.33, (double) entertainmentData.get("averageSpending"), 0.01);  // 367 / 3
        assertEquals(300.0, (double) entertainmentData.get("budgetLimit"));
        assertEquals("Exceeded", entertainmentData.get("budgetStatus"));

        // Verify transportation category
        assertTrue(result.containsKey("transportation"));
        Map<String, Object> transportationData = result.get("transportation");

        assertEquals(110.0, (double) transportationData.get("totalSpending"), 0.01);  // 50 + 60
        assertEquals(36.67, (double) transportationData.get("averageSpending"), 0.01);  // 110 / 3
        assertEquals(200.0, (double) transportationData.get("budgetLimit"));
        assertEquals("Within Budget", transportationData.get("budgetStatus"));

        // Verify housing category is not included (no expenses)
        assertFalse(result.containsKey("housing"));
    }

    @Test
    public void testCalculateSpendingTrends_WithCurrencyConversionFailure() {
        // Arrange
        when(currencyConverter.convertToBaseCurrency(eq("EUR"), anyDouble(), eq("USD")))
                .thenThrow(new CurrencyConversionException("API failure"));

        // Act
        Map<String, Map<String, Object>> result = reportService.calculateSpendingTrends(
                userId, budgets, expenses, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());  // Only 2 categories should be processed successfully

        // Verify groceries category is missing due to conversion error
        assertFalse(result.containsKey("groceries"));

        // Verify other categories are still processed
        assertTrue(result.containsKey("entertainment"));
        assertTrue(result.containsKey("transportation"));
    }

    @Test
    public void testCalculateSpendingTrends_WithEmptyExpenses() {
        // Act
        Map<String, Map<String, Object>> result = reportService.calculateSpendingTrends(
                userId, budgets, Collections.emptyList(), startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCalculateSpendingTrends_WithEmptyBudgets() {
        // Act
        Map<String, Map<String, Object>> result = reportService.calculateSpendingTrends(
                userId, Collections.emptyList(), expenses, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCalculateSpendingTrends_WithSingleDayDateRange() throws ParseException {
        // Arrange
        Date singleDay = dateFormat.parse("2023-01-15");

        // Act
        Map<String, Map<String, Object>> result = reportService.calculateSpendingTrends(
                userId, budgets, expenses, singleDay, singleDay);

        // Assert
        assertNotNull(result);

        // Verify that at least one category exists
        assertTrue(result.containsKey("groceries"));

        // Check average spending calculation for single day (should be equal to total)
        Map<String, Object> groceriesData = result.get("groceries");
        double totalSpending = (double) groceriesData.get("totalSpending");
        double averageSpending = (double) groceriesData.get("averageSpending");
        assertEquals(totalSpending, averageSpending, 0.01);
    }

    @Test
    public void testCalculateSpendingTrends_WithCaseSensitiveCategories() throws ParseException {
        // Arrange
        List<Budget> mixedCaseBudgets = new ArrayList<>();
        mixedCaseBudgets.add(createBudget("Groceries", 500.0));  // Capital G

        List<Expense> mixedCaseExpenses = new ArrayList<>();
        mixedCaseExpenses.add(createExpense("groceries", 150.0, "2023-01-15", "USD"));  // lowercase g

        // Act
        Map<String, Map<String, Object>> result = reportService.calculateSpendingTrends(
                userId, mixedCaseBudgets, mixedCaseExpenses, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        // Should match regardless of case
        assertTrue(result.containsKey("groceries"));
    }
}