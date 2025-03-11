package com.example.finance_tracker.integration;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.service.ReportServiceImpl;
import com.example.finance_tracker.util.CurrencyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Reset the database after each test
public class ReportServiceImplIntegrationTest {

    @Autowired
    private ReportServiceImpl reportService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyUtil currencyUtil;

    @BeforeEach
    public void setUp() {
        // Clear the database before each test
        budgetRepository.deleteAll();
        expenseRepository.deleteAll();
        incomeRepository.deleteAll();
//        userRepository.deleteAll();
    }

    @Test
    public void testGenerateSpendingTrendReport() {
        // Arrange
        User user = new User();
        user.setId("testuser");
        user.setBaseCurrency("USD");
        userRepository.save(user);

        Budget budget = new Budget();
        budget.setUserId("testuser");
        budget.setCategory("Groceries");
        budget.setLimit(1200.0);
        budgetRepository.save(budget); // Save the budget to the database

        Expense expense = new Expense();
        expense.setUserId("testuser");
        expense.setCategory("Groceries");
        expense.setAmount(1000.0);
        expense.setCurrencyCode("USD");
        expense.setDate(new Date(2023 - 1900, 9, 1)); // October 1, 2023
        expenseRepository.save(expense); // Save the expense to the database

        Income income = new Income();
        income.setUserId("testuser");
        income.setAmount(5000.0);
        income.setCurrencyCode("USD");
        income.setDate(new Date(2023 - 1900, 9, 1)); // October 1, 2023
        incomeRepository.save(income); // Save the income to the database

        // Verify that the expense was saved correctly
        Expense savedExpense = expenseRepository.findByUserId("testuser").get(0);
        assertNotNull(savedExpense);
        assertEquals(1000.0, savedExpense.getAmount());
        assertEquals("Groceries", savedExpense.getCategory());

        // Act
        Map<String, Object> report = reportService.generateSpendingTrendReport(
                "testuser",
                new Date(2023 - 1900, 9, 1), // October 1, 2023
                new Date(2023 - 1900, 9, 31)  // October 31, 2023
        );

        // Assert
        assertEquals("testuser", report.get("userId"));
        assertEquals(5000.0, report.get("totalIncome"));
        assertEquals(1000.0, report.get("totalExpenses"));
        assertEquals(4000.0, report.get("netSavings"));

        // Verify spending trends
        Map<String, Object> spendingTrends = (Map<String, Object>) report.get("spendingTrends");
        assertNotNull(spendingTrends);
        assertEquals(1, spendingTrends.size());

        Map<String, Object> groceriesTrend = (Map<String, Object>) spendingTrends.get("Groceries");
        assertNotNull(groceriesTrend);
        assertEquals(1000.0, groceriesTrend.get("totalSpending"));
        assertEquals(1000.0, groceriesTrend.get("averageSpending"));
        assertEquals("Within Budget", groceriesTrend.get("budgetStatus"));
    }
}