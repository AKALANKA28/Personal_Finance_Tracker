package com.example.finance_tracker.service.intergration;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.service.ExpenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest  // Uses an in-memory MongoDB instance for testing
@Import(ExpenseServiceImpl.class) // Import service for testing
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
    }

    @Test
    void updateExpense_ShouldUpdateExistingExpense() {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expense = expenseRepository.save(expense);

        expense.setAmount(150.0); // Update amount

        // Act
        Expense updatedExpense = expenseService.updateExpense(expense);

        // Assert
        assertEquals(150.0, updatedExpense.getAmount());
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
}
