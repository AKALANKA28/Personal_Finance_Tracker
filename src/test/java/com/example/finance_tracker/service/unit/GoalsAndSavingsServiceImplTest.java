package com.example.finance_tracker.service.unit;

import com.example.finance_tracker.model.*;
import com.example.finance_tracker.repository.*;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoalsAndSavingsServiceImplTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private IncomeService incomeService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GoalsAndSavingsServiceImpl goalsAndSavingsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void setGoal_Success() {
        // Arrange
        Goal goal = new Goal();
        goal.setTargetAmount(1000.0);
        goal.setDeadline(new Date(System.currentTimeMillis() + 100000000)); // Future date

        when(goalRepository.save(goal)).thenReturn(goal);

        // Act
        Goal result = goalsAndSavingsService.setGoal(goal);

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getCurrentAmount());
        assertEquals(0.0, result.getProgressPercentage());
        verify(goalRepository, times(1)).save(goal);
    }

    @Test
    void setGoal_InvalidTargetAmount_ThrowsException() {
        // Arrange
        Goal goal = new Goal();
        goal.setTargetAmount(-100.0); // Invalid amount

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            goalsAndSavingsService.setGoal(goal);
        });

        assertEquals("Target amount must be greater than 0", exception.getMessage());
    }

    @Test
    void setGoal_InvalidDeadline_ThrowsException() {
        // Arrange
        Goal goal = new Goal();
        goal.setTargetAmount(1000.0);
        goal.setDeadline(new Date(System.currentTimeMillis() - 100000000)); // Past date

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            goalsAndSavingsService.setGoal(goal);
        });

        assertEquals("Deadline cannot be in the past", exception.getMessage());
    }


    @Test
    void updateGoal_Success() {
        // Arrange
        Goal goal = new Goal();
        goal.setId("123");
        goal.setTargetAmount(1000.0);
        goal.setDeadline(new Date(System.currentTimeMillis() + 100000000)); // Future date

        when(goalRepository.existsById("123")).thenReturn(true);
        when(goalRepository.save(goal)).thenReturn(goal);

        // Act
        Goal result = goalsAndSavingsService.updateGoal(goal);

        // Assert
        assertNotNull(result);
        verify(goalRepository, times(1)).save(goal);
    }

    @Test
    void updateGoal_NotFound_ThrowsException() {
        // Arrange
        Goal goal = new Goal();
        goal.setId("123");

        when(goalRepository.existsById("123")).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            goalsAndSavingsService.updateGoal(goal);
        });

        assertEquals("Goal not found", exception.getMessage());
    }

    @Test
    void deleteGoal_Success() {
        // Arrange
        String goalId = "123";

        when(goalRepository.existsById(goalId)).thenReturn(true);
        doNothing().when(goalRepository).deleteById(goalId);

        // Act
        boolean result = goalsAndSavingsService.deleteGoal(goalId);

        // Assert
        assertTrue(result);
        verify(goalRepository, times(1)).deleteById(goalId);
    }

    @Test
    void deleteGoal_NotFound_ThrowsException() {
        // Arrange
        String goalId = "123";

        when(goalRepository.existsById(goalId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            goalsAndSavingsService.deleteGoal(goalId);
        });

        assertEquals("Goal not found", exception.getMessage());
    }

    @Test
    void getGoalsByUser_Success() {
        // Arrange
        String userId = "user123";
        Goal goal = new Goal();
        goal.setUserId(userId);

        when(goalRepository.findByUserId(userId)).thenReturn(Collections.singletonList(goal));

        // Act
        List<Goal> result = goalsAndSavingsService.getGoalsByUser(userId);

        // Assert
        assertEquals(1, result.size());
        verify(goalRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getGoalById_Success() {
        // Arrange
        String goalId = "123";
        Goal goal = new Goal();
        goal.setId(goalId);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));

        // Act
        Goal result = goalsAndSavingsService.getGoalById(goalId);

        // Assert
        assertNotNull(result);
        assertEquals(goalId, result.getId());
    }

    @Test
    void getGoalById_NotFound_ThrowsException() {
        // Arrange
        String goalId = "123";

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            goalsAndSavingsService.getGoalById(goalId);
        });

        assertEquals("Goal not found", exception.getMessage());
    }

    @Test
    void addManualContribution_Success() {
        // Arrange
        String goalId = "123";
        Goal goal = new Goal();
        goal.setId(goalId);
        goal.setTargetAmount(1000.0);
        goal.setCurrentAmount(500.0);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);

        // Act
        Goal result = goalsAndSavingsService.addManualContribution(goalId, 200.0);

        // Assert
        assertNotNull(result);
        assertEquals(700.0, result.getCurrentAmount());
        assertEquals(70.0, result.getProgressPercentage());
        verify(goalRepository, times(1)).save(goal);
    }

//    @Test
//    void trackGoalProgress_Success() {
//        // Arrange
//        String goalId = "123";
//        String userId = "user123";
//        Goal goal = new Goal();
//        goal.setId(goalId);
//        goal.setUserId(userId);
//        goal.setTargetAmount(1000.0);
//        goal.setCurrentAmount(500.0);
//        goal.setDeadline(new Date(System.currentTimeMillis() + 100000000)); // Future date
//
//        // Mock user
//        User user = new User();
//        user.setId(userId);
//        user.setEmail("user@example.com");
//
//        // Mock transactions
//        Transaction transaction = new Transaction();
//        transaction.setUserId(userId);
//        transaction.setAmount(200.0);
//        transaction.setCategory("Goals");
//        transaction.setGoalId(goalId);
//
//        // Mock budget
//        Budget budget = new Budget();
//        budget.setUserId(userId);
//        budget.setLimit(300.0);
//        budget.setGoalId(goalId);
//
//        // Mock repository and service calls
//        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
//        when(transactionService.getTransactionsByUser(userId)).thenReturn(Collections.singletonList(transaction));
//        when(budgetRepository.findByGoalId(goalId)).thenReturn(Optional.of(budget));
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user)); // Mock userRepository
//        when(goalRepository.save(goal)).thenReturn(goal);
//
//        // Act
//        Goal result = goalsAndSavingsService.trackGoalProgress(goalId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1000.0, result.getCurrentAmount()); // 500 (initial) + 200 (transactions) + 300 (budget)
//        assertEquals(100.0, result.getProgressPercentage()); // (1000 / 1000) * 100
//        verify(goalRepository, times(1)).save(goal);
//
//        // Verify notification was sent
//        verify(notificationService, times(1)).sendNotification(any(Notification.class));
//        verify(notificationService, times(1)).sendEmailNotification(any(Notification.class));
//    }


    @Test
    void calculateNetSavings_Success() throws ParseException {
        // Arrange
        String userId = "user123";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = dateFormat.parse("2023-01-01");
        Date endDate = dateFormat.parse("2023-12-31");


        when(incomeService.calculateTotalIncomeInBaseCurrency(userId)).thenReturn(10000.0);
        when(expenseService.calculateTotalExpensesInBaseCurrency(userId)).thenReturn(8000.0);

        // Act
        double result = goalsAndSavingsService.calculateNetSavings(userId, startDate, endDate);

        // Assert
        assertEquals(2000.0, result);
    }

//    @Test
//    void allocateSavings_Success() {
//        // Arrange
//        String userId = "user123";
//        double amount = 1000.0;
//
//        Goal goal1 = new Goal();
//        goal1.setTargetAmount(2000.0);
//        Goal goal2 = new Goal();
//        goal2.setTargetAmount(3000.0);
//
//        when(goalRepository.findByUserIdAndDeadlineAfter(userId, new Date()))
//                .thenReturn(Arrays.asList(goal1, goal2));
//        doNothing().when(transactionService).addTransaction(any(Transaction.class));
//
//        // Act
//        goalsAndSavingsService.allocateSavings(userId, amount);
//
//        // Assert
//        verify(transactionService, times(2)).addTransaction(any(Transaction.class));
//    }

    @Test
    void calculateTotalSavings_Success() {
        // Arrange
        String userId = "user123";
        Transaction transaction = new Transaction();
        transaction.setAmount(500.0);
        transaction.setCategory("Savings");

        when(transactionService.getTransactionsByUser(userId)).thenReturn(Collections.singletonList(transaction));

        // Act
        double result = goalsAndSavingsService.calculateTotalSavings(userId);

        // Assert
        assertEquals(500.0, result);
    }

    @Test
    void calculateRemainingAmountForGoal_Success() {
        // Arrange
        String goalId = "123";
        Goal goal = new Goal();
        goal.setId(goalId);
        goal.setUserId("user123");
        goal.setTargetAmount(1000.0);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(transactionService.getTransactionsByUser("user123")).thenReturn(Collections.emptyList());

        // Act
        double result = goalsAndSavingsService.calculateRemainingAmountForGoal(goalId);

        // Assert
        assertEquals(1000.0, result);
    }

    @Test
    void getActiveGoals_Success() {
        // Arrange
        String userId = "user123";
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setDeadline(new Date(System.currentTimeMillis() + 100000000)); // Future date

        when(goalRepository.findByUserIdAndDeadlineAfter(userId, new Date()))
                .thenReturn(Collections.singletonList(goal));

        // Act
        List<Goal> result = goalsAndSavingsService.getActiveGoals(userId);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void getCompletedGoals_Success() {
        // Arrange
        String userId = "user123";
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setProgressPercentage(100.0);

        when(goalRepository.findByUserIdAndProgressPercentageGreaterThanEqual(userId, 100))
                .thenReturn(Collections.singletonList(goal));

        // Act
        List<Goal> result = goalsAndSavingsService.getCompletedGoals(userId);

        // Assert
        assertEquals(1, result.size());
    }


    @Test
    void getOverdueGoals_Success() {
        // Arrange
        String userId = "user123";
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setDeadline(new Date(System.currentTimeMillis() - 100000000)); // Past date
        goal.setProgressPercentage(50.0);

        when(goalRepository.findByUserIdAndDeadlineBeforeAndProgressPercentageLessThan(userId, new Date(), 100))
                .thenReturn(Collections.singletonList(goal));

        // Act
        List<Goal> result = goalsAndSavingsService.getOverdueGoals(userId);

        // Assert
        assertEquals(1, result.size());
    }


    @Test
    void linkBudgetToGoal_Success() {
        // Arrange
        String goalId = "123";
        String budgetId = "456";
        Goal goal = new Goal();
        goal.setId(goalId);
        goal.setUserId("user123");

        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setUserId("user123");

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(goalRepository.save(goal)).thenReturn(goal);

        // Act
        goalsAndSavingsService.linkBudgetToGoal(goalId, budgetId);

        // Assert
        assertEquals(budgetId, goal.getBudgetId());
        verify(goalRepository, times(1)).save(goal);
    }

    @Test
    void unlinkBudgetFromGoal_Success() {
        // Arrange
        String goalId = "123";
        Goal goal = new Goal();
        goal.setId(goalId);
        goal.setBudgetId("456");

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);

        // Act
        goalsAndSavingsService.unlinkBudgetFromGoal(goalId);

        // Assert
        assertNull(goal.getBudgetId());
        verify(goalRepository, times(1)).save(goal);
    }
}