package com.example.finance_tracker.service.integration;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.service.ExpenseServiceImpl;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExpenseServiceImplIntegrationTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseServiceImpl expenseService;

    @BeforeEach
    void clearDatabase() {
        expenseRepository.deleteAll(); // Clear database before each test
    }

    @Test
    void addExpense_ShouldSaveExpense() {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");

        // Act
        Expense savedExpense = expenseService.addExpense(expense);

        // Assert
        assertNotNull(savedExpense.getId());
        assertEquals("user123", savedExpense.getUserId());
        assertEquals(100.0, savedExpense.getAmount());
        assertEquals("USD", savedExpense.getCurrencyCode());
        assertEquals("Food", savedExpense.getCategory());
    }

    @Test
    void testExpenseRepository() {
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expense = expenseRepository.save(expense);

        assertNotNull(expense.getId(), "Expense ID should not be null after saving");

        Expense retrievedExpense = expenseRepository.findById(expense.getId()).orElse(null);
        assertNotNull(retrievedExpense, "Expense should be retrievable from the repository");
        assertEquals("user123", retrievedExpense.getUserId());
    }

    @Test
    void updateExpense_ShouldUpdateExistingExpense() {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expense = expenseRepository.save(expense); // Save the expense

        assertNotNull(expense, "Expense should not be null after saving"); // Verify expense is saved

        expense.setAmount(150.0); // Update amount
        expense.setCategory("Groceries"); // Update category

        // Act
        Expense updatedExpense = expenseService.updateExpense(expense);

        // Assert
        assertNotNull(updatedExpense, "Updated expense should not be null");
        assertEquals(150.0, updatedExpense.getAmount());
        assertEquals("Groceries", updatedExpense.getCategory());
    }

    @Test
    void updateExpense_ShouldThrowResourceNotFoundException_WhenExpenseNotFound() {
        // Arrange
        Expense expense = new Expense();
        expense.setId("nonExistentId");
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> expenseService.updateExpense(expense));
    }

    @Test
    void deleteExpense_ShouldRemoveExpense() {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expense = expenseRepository.save(expense);

        // Act
        boolean isDeleted = expenseService.deleteExpense(expense.getId());

        // Assert
        assertTrue(isDeleted);
        assertFalse(expenseRepository.existsById(expense.getId()));
    }

    @Test
    void deleteExpense_ShouldThrowResourceNotFoundException_WhenExpenseNotFound() {
        // Arrange
        String nonExistentId = "nonExistentId";

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> expenseService.deleteExpense(nonExistentId));
    }

    @Test
    void getExpensesByUser_ShouldReturnUserExpenses() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setUserId("user123");
        expense1.setAmount(100.0);
        expense1.setCurrencyCode("USD");
        expense1.setCategory("Food");
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setUserId("user456");
        expense2.setAmount(200.0);
        expense2.setCurrencyCode("USD");
        expense2.setCategory("Transport");
        expenseRepository.save(expense2);

        // Act
        List<Expense> userExpenses = expenseService.getExpensesByUser("user123");

        // Assert
        assertEquals(1, userExpenses.size());
        assertEquals("Food", userExpenses.get(0).getCategory());
    }

    @Test
    void getExpensesByUserAndCategory_ShouldReturnFilteredExpenses() {
        // Arrange
        Expense expense1 = new Expense();
        expense1.setUserId("user123");
        expense1.setAmount(100.0);
        expense1.setCurrencyCode("USD");
        expense1.setCategory("Food");
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setUserId("user123");
        expense2.setAmount(200.0);
        expense2.setCurrencyCode("USD");
        expense2.setCategory("Transport");
        expenseRepository.save(expense2);

        // Act
        List<Expense> filteredExpenses = expenseService.getExpensesByUserAndCategory("user123", "Food");

        // Assert
        assertEquals(1, filteredExpenses.size());
        assertEquals("Food", filteredExpenses.get(0).getCategory());
    }

    @Test
    void isOwner_ShouldReturnTrueIfUserIsOwner() {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expense = expenseRepository.save(expense);

        // Act & Assert
        assertTrue(expenseService.isOwner(expense.getId(), "user123"));
        assertFalse(expenseService.isOwner(expense.getId(), "user456"));
    }

    @Test
    void isOwner_ShouldThrowResourceNotFoundException_WhenExpenseNotFound() {
        // Arrange
        String nonExistentId = "nonExistentId";

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> expenseService.isOwner(nonExistentId, "user123"));
    }
}