package com.example.finance_tracker.integration.controller;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll(); // Clear the database before each test
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void addExpense_ShouldReturnCreatedExpense() throws Exception {
        // Arrange
        String expenseJson = "{\"userId\":\"user123\",\"amount\":100.0,\"currencyCode\":\"USD\",\"category\":\"Food\"}";

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                        .contentType("application/json")
                        .content(expenseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void updateExpense_ShouldReturnUpdatedExpense() throws Exception {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expense = expenseRepository.save(expense);

        String updatedExpenseJson = "{\"id\":\"" + expense.getId() + "\",\"userId\":\"user123\",\"amount\":150.0,\"currencyCode\":\"USD\",\"category\":\"Food\"}";

        // Act & Assert
        mockMvc.perform(put("/api/expenses/{id}", expense.getId())
                        .contentType("application/json")
                        .content(updatedExpenseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.0));
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void deleteExpense_ShouldReturnNoContent() throws Exception {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expense = expenseRepository.save(expense);

        // Act & Assert
        mockMvc.perform(delete("/api/expenses/{id}", expense.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getExpensesByUser_ShouldReturnUserExpenses() throws Exception {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expenseRepository.save(expense);

        // Act & Assert
        mockMvc.perform(get("/api/expenses/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Food"));
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getExpensesByUserAndCategory_ShouldReturnFilteredExpenses() throws Exception {
        // Arrange
        Expense expense = new Expense();
        expense.setUserId("user123");
        expense.setAmount(100.0);
        expense.setCurrencyCode("USD");
        expense.setCategory("Food");
        expenseRepository.save(expense);

        // Act & Assert
        mockMvc.perform(get("/api/expenses/user/user123/category/Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Food"));
    }
}