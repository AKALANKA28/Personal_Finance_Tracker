package com.example.finance_tracker.service.integration;

import com.example.finance_tracker.model.*;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.service.*;
import com.example.finance_tracker.util.CurrencyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BudgetServiceImplIntegrationTest {


    private static final Logger logger = LoggerFactory.getLogger(BudgetServiceImplIntegrationTest.class);

    @Autowired
    private BudgetServiceImpl budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CurrencyConverterImpl currencyConverter;

    @Mock
    private GoalsAndSavingsService goalsAndSavingsService;

    @Mock
    private CurrencyUtil currencyUtil;

    private Budget budget;
    private final String userId = "testUser123";

    @BeforeEach
    void setUp() {
        logger.info("Setting up test data...");

        // Clear the budgets collection before each test
        mongoTemplate.dropCollection(Budget.class);
        logger.info("Dropped Budget collection.");

        // Mock the currency util to return a default currency
        when(currencyUtil.getBaseCurrencyForUser(anyString())).thenAnswer(invocation -> {
            String requestedUserId = invocation.getArgument(0);
            logger.info("Mocked getBaseCurrencyForUser called for user: {}", requestedUserId);
            return "USD";
        });

        // Create a test budget
        budget = new Budget();
        budget.setUserId(userId);
        budget.setCategory("Groceries");
        budget.setLimit(500.0);
        budget.setCurrencyCode("USD");
        budget.setStartDate(new Date());

        logger.info("Test budget created: {}", budget);
    }

    @Test
    void setBudgetTest_Success() {
        logger.info("Starting test: setBudget_Success");

        // Act
        try {
            logger.info("Calling budgetService.setBudget...");
            Budget savedBudget = budgetService.setBudget(budget);

            // Assert
            assertNotNull(savedBudget);
            assertNotNull(savedBudget.getId());
            assertEquals(userId, savedBudget.getUserId());
            assertEquals("Groceries", savedBudget.getCategory());
            assertEquals(500.0, savedBudget.getLimit());
            assertEquals("USD", savedBudget.getCurrencyCode());

            logger.info("Budget saved successfully: {}", savedBudget);

            // Verify with MongoTemplate
            Query query = new Query(Criteria.where("id").is(savedBudget.getId()));
            Budget foundBudget = mongoTemplate.findOne(query, Budget.class);

            assertNotNull(foundBudget);
            assertEquals(userId, foundBudget.getUserId());
            assertEquals("Groceries", foundBudget.getCategory());

            logger.info("Budget found in MongoDB: {}", foundBudget);
        } catch (Exception e) {
            logger.error("Exception occurred during setBudget_Success test: ", e);
            throw e;  // Rethrow for proper test failure reporting
        }
    }

    @Test
    void updateBudget_Success() {
        // Arrange
        Budget savedBudget = mongoTemplate.save(budget);
        savedBudget.setLimit(600.0);
        savedBudget.setCategory("Updated Groceries");

        // Act
        Budget updatedBudget = budgetService.updateBudget(savedBudget);

        // Assert
        assertNotNull(updatedBudget);
        assertEquals(savedBudget.getId(), updatedBudget.getId());
        assertEquals(600.0, updatedBudget.getLimit());
        assertEquals("Updated Groceries", updatedBudget.getCategory());

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("id").is(savedBudget.getId()));
        Budget foundBudget = mongoTemplate.findOne(query, Budget.class);

        assertNotNull(foundBudget);
        assertEquals(600.0, foundBudget.getLimit());
        assertEquals("Updated Groceries", foundBudget.getCategory());
    }

    @Test
    void deleteBudget_Success() {
        // Arrange
        Budget savedBudget = mongoTemplate.save(budget);

        // Act
        boolean result = budgetService.deleteBudget(savedBudget.getId());

        // Assert
        assertTrue(result);

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("id").is(savedBudget.getId()));
        Budget foundBudget = mongoTemplate.findOne(query, Budget.class);
        assertNull(foundBudget);
    }

    @Test
    void getBudgetsByUser_Success() {
        // Arrange
        Budget budget1 = new Budget();
        budget1.setUserId(userId);
        budget1.setCategory("Groceries");
        budget1.setLimit(500.0);

        Budget budget2 = new Budget();
        budget2.setUserId(userId);
        budget2.setCategory("Entertainment");
        budget2.setLimit(200.0);

        mongoTemplate.save(budget1);
        mongoTemplate.save(budget2);

        // Create a budget for a different user that shouldn't be returned
        Budget otherUserBudget = new Budget();
        otherUserBudget.setUserId("otherUser");
        otherUserBudget.setCategory("Food");
        otherUserBudget.setLimit(300.0);
        mongoTemplate.save(otherUserBudget);

        // Act
        List<Budget> budgets = budgetService.getBudgetsByUser(userId);

        // Assert
        assertEquals(2, budgets.size());
        assertTrue(budgets.stream().anyMatch(b -> b.getCategory().equals("Groceries")));
        assertTrue(budgets.stream().anyMatch(b -> b.getCategory().equals("Entertainment")));

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("userId").is(userId));
        List<Budget> foundBudgets = mongoTemplate.find(query, Budget.class);
        assertEquals(2, foundBudgets.size());
    }

    @Test
    void checkBudgetExceeded_BudgetNearingLimit() {
        // Arrange
        Budget savedBudget = mongoTemplate.save(budget);

        // Mock the expenses to be 80% of the budget
        List<Expense> expenses = Arrays.asList(
                createExpense(userId, "Groceries", 400.0)
        );

        // Setup calendar for date ranges
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();

        when(expenseRepository.findByUserIdAndCategoryAndDateBetween(
                eq(userId), eq("Groceries"), any(Date.class), any(Date.class)
        )).thenReturn(expenses);

        when(currencyConverter.convertCurrency(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(400.0); // 80% of budget

        // Act
        budgetService.checkBudgetExceeded(userId);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).sendNotification(notificationCaptor.capture());

        Notification sentNotification = notificationCaptor.getValue();
        assertEquals(userId, sentNotification.getUserId());
        assertEquals("Budget Nearing Limit", sentNotification.getTitle());
        assertTrue(sentNotification.getMessage().contains("nearing your budget"));
    }

    @Test
    void checkBudgetExceeded_BudgetExceeded() {
        // Arrange
        Budget savedBudget = mongoTemplate.save(budget);

        // Mock the expenses to exceed the budget
        List<Expense> expenses = Arrays.asList(
                createExpense(userId, "Groceries", 600.0)
        );

        when(expenseRepository.findByUserIdAndCategoryAndDateBetween(
                eq(userId), eq("Groceries"), any(Date.class), any(Date.class)
        )).thenReturn(expenses);

        when(currencyConverter.convertCurrency(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(600.0); // 120% of budget

        // Act
        budgetService.checkBudgetExceeded(userId);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).sendNotification(notificationCaptor.capture());

        Notification sentNotification = notificationCaptor.getValue();
        assertEquals(userId, sentNotification.getUserId());
        assertEquals("Budget Exceeded", sentNotification.getTitle());
        assertTrue(sentNotification.getMessage().contains("has been exceeded"));
    }

    @Test
    void provideBudgetAdjustmentRecommendations_Success() {
        // Arrange
        Budget savedBudget = mongoTemplate.save(budget);

        // Mock expenses for the past 3 months
        List<Expense> expenses = Arrays.asList(
                createExpense(userId, "Groceries", 300.0) // Under budget
        );

        when(expenseRepository.findByUserIdAndDateBetween(
                eq(userId), any(Date.class), any(Date.class)
        )).thenReturn(expenses);

        when(currencyConverter.convertToBaseCurrency(anyString(), anyDouble(), anyString()))
                .thenReturn(300.0); // Under budget by more than 10%

        when(goalsAndSavingsService.calculateNetSavings(eq(userId), any(Date.class), any(Date.class)))
                .thenReturn(1000.0);

        // Act
        budgetService.provideBudgetAdjustmentRecommendations(userId);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).sendNotification(notificationCaptor.capture());

        Notification sentNotification = notificationCaptor.getValue();
        assertEquals(userId, sentNotification.getUserId());
        assertEquals("Budget Adjustment Recommendations", sentNotification.getTitle());
        assertTrue(sentNotification.getMessage().contains("decreasing your budget"));
        assertTrue(sentNotification.getMessage().contains("net savings"));
    }

    @Test
    void allocateBudgetToGoal_Success() {
        // Arrange
        Budget savingsBudget = new Budget();
        savingsBudget.setUserId(userId);
        savingsBudget.setCategory("Savings");
        savingsBudget.setLimit(1000.0);
        savingsBudget.setCurrencyCode("USD");
        Budget savedBudget = mongoTemplate.save(savingsBudget);

        String goalId = "goal123";
        Goal mockGoal = new Goal();
        mockGoal.setId(goalId);
        mockGoal.setUserId(userId);
        mockGoal.setName("Vacation");
        mockGoal.setTargetAmount(2000.0);
        mockGoal.setCurrentAmount(500.0);

        when(goalsAndSavingsService.getGoalById(goalId)).thenReturn(mockGoal);
        when(budgetRepository.findByUserIdAndCategory(userId, "Savings"))
                .thenReturn(java.util.Optional.of(savedBudget));

        double allocationAmount = 300.0;

        // Act
        budgetService.allocateBudgetToGoal(userId, goalId, allocationAmount);

        // Assert
        // Verify budget was updated
        Query query = new Query(Criteria.where("id").is(savedBudget.getId()));
        Budget updatedBudget = mongoTemplate.findOne(query, Budget.class);

        assertNotNull(updatedBudget);
        assertEquals(700.0, updatedBudget.getLimit()); // 1000 - 300

        // Verify goal was updated
        verify(goalsAndSavingsService).updateGoal(argThat(goal ->
                goal.getId().equals(goalId) &&
                        goal.getCurrentAmount() == 800.0 // 500 + 300
        ));

        // Verify notification was sent
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).sendNotification(notificationCaptor.capture());

        Notification sentNotification = notificationCaptor.getValue();
        assertEquals(userId, sentNotification.getUserId());
        assertEquals("Budget Allocation", sentNotification.getTitle());
        assertTrue(sentNotification.getMessage().contains("Allocated 300.0"));
    }

    @Test
    void allocateBudgetToGoal_ExceedsBudgetLimit_ThrowsException() {
        // Arrange
        Budget savingsBudget = new Budget();
        savingsBudget.setUserId(userId);
        savingsBudget.setCategory("Savings");
        savingsBudget.setLimit(200.0);
        savingsBudget.setCurrencyCode("USD");
        Budget savedBudget = mongoTemplate.save(savingsBudget);

        String goalId = "goal123";
        Goal mockGoal = new Goal();
        mockGoal.setId(goalId);
        mockGoal.setUserId(userId);
        mockGoal.setName("Vacation");

        when(goalsAndSavingsService.getGoalById(goalId)).thenReturn(mockGoal);
        when(budgetRepository.findByUserIdAndCategory(userId, "Savings"))
                .thenReturn(java.util.Optional.of(savedBudget));

        double allocationAmount = 300.0; // Exceeds budget limit of 200.0

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            budgetService.allocateBudgetToGoal(userId, goalId, allocationAmount);
        });

        assertTrue(exception.getMessage().contains("Allocation amount exceeds budget limit"));

        // Verify budget was not changed
        Query query = new Query(Criteria.where("id").is(savedBudget.getId()));
        Budget unchangedBudget = mongoTemplate.findOne(query, Budget.class);
        assertEquals(200.0, unchangedBudget.getLimit());
    }

    @Test
    void isOwner_Success() {
        // Arrange
        Budget savedBudget = mongoTemplate.save(budget);

        // Act
        boolean result = budgetService.isOwner(savedBudget.getId(), userId);

        // Assert
        assertTrue(result);

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("id").is(savedBudget.getId())
                .and("userId").is(userId));
        Budget foundBudget = mongoTemplate.findOne(query, Budget.class);
        assertNotNull(foundBudget);
    }

    @Test
    void isOwner_NotOwner_ReturnsFalse() {
        // Arrange
        Budget savedBudget = mongoTemplate.save(budget);
        String otherUserId = "otherUser456";

        // Act
        boolean result = budgetService.isOwner(savedBudget.getId(), otherUserId);

        // Assert
        assertFalse(result);

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("id").is(savedBudget.getId())
                .and("userId").is(otherUserId));
        Budget foundBudget = mongoTemplate.findOne(query, Budget.class);
        assertNull(foundBudget);
    }

    @Test
    void isOwner_BudgetNotFound_ThrowsException() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.isOwner("nonExistentId", userId);
        });

        assertEquals("Budget not found", exception.getMessage());

        // Verify with MongoTemplate
        Query query = new Query(Criteria.where("id").is("nonExistentId"));
        Budget foundBudget = mongoTemplate.findOne(query, Budget.class);
        assertNull(foundBudget);
    }

    // Helper method to create an expense
    private Expense createExpense(String userId, String category, double amount) {
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setCategory(category);
        expense.setAmount(amount);
        expense.setCurrencyCode("USD");
        expense.setDate(new Date());
        return expense;
    }
}