package com.example.finance_tracker.service.unit;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.service.CurrencyConverterImpl;
import com.example.finance_tracker.service.ExpenseServiceImpl;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CurrencyConverterImpl currencyConverterImpl;

    @Mock
    private CurrencyUtil currencyUtil;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addExpense_Success() {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");

        when(expenseRepository.save(expense)).thenReturn(expense);

        // Act
        Expense result = expenseService.addExpense(expense);

        // Assert
        assertNotNull(result);
        verify(expenseRepository, times(1)).save(expense);
    }


    @Test
    void updateExpense_Success() {
        // Arrange
        Expense expense = new Expense();
        expense.setId("123");
        expense.setUserId("user123");

        when(expenseRepository.existsById("123")).thenReturn(true);
        when(expenseRepository.save(expense)).thenReturn(expense);

        // Act
        Expense result = expenseService.updateExpense(expense);

        // Assert
        assertNotNull(result);
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void updateExpense_NotFound_ThrowsException() {
        // Arrange
        Expense expense = new Expense();
        expense.setId("123");

        when(expenseRepository.existsById("123")).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.updateExpense(expense);
        });

        assertEquals("Expense not found", exception.getMessage());
    }



    @Test
    void deleteExpense_Success() {
        // Arrange
        String expenseId = "123";

        when(expenseRepository.existsById(expenseId)).thenReturn(true);
        doNothing().when(expenseRepository).deleteById(expenseId);

        // Act
        boolean result = expenseService.deleteExpense(expenseId);

        // Assert
        assertTrue(result);
        verify(expenseRepository, times(1)).deleteById(expenseId);
    }

    @Test
    void deleteExpense_NotFound_ThrowsException() {
        // Arrange
        String expenseId = "123";

        when(expenseRepository.existsById(expenseId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.deleteExpense(expenseId);
        });

        assertEquals("Expense not found", exception.getMessage());
    }


    @Test
    void getExpensesByUser_Success() {
        // Arrange
        String userId = "user123";
        Expense expense = new Expense();
        expense.setUserId(userId);

        when(expenseRepository.findByUserId(userId)).thenReturn(Collections.singletonList(expense));

        // Act
        List<Expense> result = expenseService.getExpensesByUser(userId);

        // Assert
        assertEquals(1, result.size());
        verify(expenseRepository, times(1)).findByUserId(userId);
    }


    @Test
    void getExpensesByUserInPreferredCurrency_Success() {
        // Arrange
        String userId = "user123";
        String preferredCurrency = "EUR";
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setCurrencyCode("USD");
        expense.setAmount(100.0);

        when(expenseRepository.findByUserId(userId)).thenReturn(Collections.singletonList(expense));
        when(currencyUtil.getBaseCurrencyForUser(userId)).thenReturn("USD");
        when(currencyConverterImpl.convertCurrency("USD", preferredCurrency, 100.0, "USD")).thenReturn(85.0);

        // Act
        List<Expense> result = expenseService.getExpensesByUserInPreferredCurrency(userId, preferredCurrency);

        // Assert
        assertEquals(1, result.size());
        assertEquals(85.0, result.get(0).getAmount());
        assertEquals("EUR", result.get(0).getCurrencyCode());
    }


    @Test
    void convertExpenseToPreferredCurrency_Success() {
        // Arrange
        Expense expense = new Expense();
        expense.setCurrencyCode("USD");
        expense.setAmount(100.0);
        expense.setUserId("user123");

        when(currencyUtil.getBaseCurrencyForUser("user123")).thenReturn("USD");
        when(currencyConverterImpl.convertCurrency("USD", "EUR", 100.0, "USD")).thenReturn(85.0);

        // Act
        Expense result = expenseService.convertExpenseToPreferredCurrency(expense, "EUR");

        // Assert
        assertEquals(85.0, result.getAmount());
        assertEquals("EUR", result.getCurrencyCode());
    }

    @Test
    void getExpensesByUserAndCategory_Success() {
        // Arrange
        String userId = "user123";
        String category = "Food";
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setCategory(category);

        when(expenseRepository.findByUserIdAndCategory(userId, category)).thenReturn(Collections.singletonList(expense));

        // Act
        List<Expense> result = expenseService.getExpensesByUserAndCategory(userId, category);

        // Assert
        assertEquals(1, result.size());
        verify(expenseRepository, times(1)).findByUserIdAndCategory(userId, category);
    }





    @Test
    void calculateTotalExpensesInBaseCurrency_Success() {
        // Arrange
        String userId = "user123";
        Expense expense = new Expense();
        expense.setCurrencyCode("USD");
        expense.setAmount(100.0);

        when(expenseRepository.findByUserId(userId)).thenReturn(Collections.singletonList(expense));
        when(currencyUtil.getBaseCurrencyForUser(userId)).thenReturn("USD");
        when(currencyConverterImpl.convertToBaseCurrency("USD", 100.0, "USD")).thenReturn(100.0);

        // Act
        double result = expenseService.calculateTotalExpensesInBaseCurrency(userId);

        // Assert
        assertEquals(100.0, result);
    }

//
//    @Test
//    void getExpenseById_Success() {
//        // Arrange
//        String expenseId = "123";
//        Expense expense = new Expense();
//        expense.setId(expenseId);
//        expense.setUserId("user123");
//        expense.setAmount(100.0);
//        expense.setCurrencyCode("USD");
//
//        // Mock the repository method to return the expense
//        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
//
//        // Act
//        Expense result = expenseService.getExpenseById(expenseId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(expenseId, result.getId());
//        assertEquals("user123", result.getUserId());
//        assertEquals(100.0, result.getAmount());
//        assertEquals("USD", result.getCurrencyCode());
//
//        // Verify the repository method was called with the correct parameter
//        verify(expenseRepository, times(1)).findById(expenseId);
//    }


//    @Test
//    void getExpenseById_NotFound_ThrowsException() {
//        // Arrange
//        String expenseId = "123";
//
//        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            expenseService.getExpenseById(expenseId);
//        });
//
//        assertEquals("Expense not found", exception.getMessage());
//    }

}