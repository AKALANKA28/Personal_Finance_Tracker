package com.example.finance_tracker.unit;

import com.example.finance_tracker.model.*;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.service.BudgetServiceImpl;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

//    @Mock
//    private NotificationService notificationService;
//
//    @Mock
//    private ExpenseRepository expenseRepository;
//
//    @Mock
//    private CurrencyConverterImpl currencyConverterImpl;
//
//    @Mock
//    private GoalsAndSavingsService goalsAndSavingsService;
//
//    @Mock
//    private CurrencyUtil currencyUtil;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private String userId;
    private String budgetId;
    private Budget budget;
    private Expense expense;
    private Goal goal;

    @BeforeEach
    public void setUp() {
        userId = "user123";
        budgetId = "budget123";

        budget = new Budget();
        budget.setId(budgetId);
        budget.setUserId(userId);
        budget.setCategory("Groceries");
        budget.setLimit(1000.0);
        budget.setCurrencyCode("USD");

        expense = new Expense();
        expense.setId("expense123");
        expense.setUserId(userId);
        expense.setCategory("Groceries");
        expense.setAmount(500.0);
        expense.setCurrencyCode("USD");
        expense.setDate(java.sql.Date.valueOf(LocalDate.now()));

        goal = new Goal();
        goal.setId("goal123");
        goal.setUserId(userId);
        goal.setName("Vacation Fund");
        goal.setTargetAmount(5000.0);
        goal.setCurrentAmount(1000.0);
    }

//    @Test
//    public void testCheckBudgetExceeded() {
//        // Arrange
//        when(budgetRepository.findByUserId(userId)).thenReturn(Collections.singletonList(budget));
//        when(expenseRepository.findByUserIdAndCategoryAndDateBetween(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class)))
//                .thenReturn(Collections.singletonList(expense));
//
//        // Use isNull() for the baseCurrency parameter
//        when(currencyConverter.convertCurrency(eq("USD"), eq("USD"), eq(500.0), isNull()))
//                .thenReturn(500.0);
//
//        // Act
//        budgetService.checkBudgetExceeded(userId);
//
//        // Assert
//        verify(notificationService, times(1)).sendNotification(any(Notification.class));
//    }
//    @Test
//    public void testProvideBudgetAdjustmentRecommendations() {
//        // Arrange
//        when(budgetRepository.findByUserId(userId)).thenReturn(Collections.singletonList(budget));
//        when(expenseRepository.findByUserIdAndDateBetween(any(), any(), any()))
//                .thenReturn(Collections.singletonList(expense));
//        when(currencyConverter.convertToBaseCurrency(any(), any(), any())).thenReturn(500.0);
//        when(goalsAndSavingsService.calculateNetSavings(any(), any(), any())).thenReturn(2000.0);
//
//        // Act
//        budgetService.provideBudgetAdjustmentRecommendations(userId);
//
//        // Assert
//        verify(notificationService, times(1)).sendNotification(any(Notification.class));
//    }

//    @Test
//    public void testAllocateBudgetToGoal_Success() {
//        // Arrange
//        when(budgetRepository.findByUserIdAndCategory(userId, "Savings"))
//                .thenReturn(Optional.of(budget));
//        when(goalsAndSavingsService.getGoalById(goal.getId())).thenReturn(goal);
//
//        // Act
//        budgetService.allocateBudgetToGoal(userId, goal.getId(), 200.0);
//
//        // Assert
//        verify(budgetRepository, times(1)).save(budget);
//        verify(goalsAndSavingsService, times(1)).updateGoal(goal);
//        verify(notificationService, times(1)).sendNotification(any(Notification.class));
//    }
//
    @Test
    public void testAllocateBudgetToGoal_ExceedsLimit() {
        // Arrange
        when(budgetRepository.findByUserIdAndCategory(userId, "Savings"))
                .thenReturn(Optional.of(budget));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            budgetService.allocateBudgetToGoal(userId, goal.getId(), 1200.0);
        });
    }

    @Test
    public void testIsOwner_True() {
        // Arrange
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        // Act
        boolean isOwner = budgetService.isOwner(budgetId, userId);

        // Assert
        assertTrue(isOwner);
    }

    @Test
    public void testIsOwner_False() {
        // Arrange
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        // Act
        boolean isOwner = budgetService.isOwner(budgetId, "anotherUser");

        // Assert
        assertFalse(isOwner);
    }

    @Test
    public void testIsOwner_BudgetNotFound() {
        // Arrange
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            budgetService.isOwner(budgetId, userId);
        });
    }
}