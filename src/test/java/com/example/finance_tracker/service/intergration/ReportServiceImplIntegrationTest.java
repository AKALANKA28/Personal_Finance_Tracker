package com.example.finance_tracker.service.intergration;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.service.*;
import com.example.finance_tracker.util.CurrencyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
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

    @Mock
    private CurrencyUtil currencyUtil;

    @Mock
    private CurrencyConverter currencyConverterImpl;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private GoalsAndSavingsService goalsAndSavingsService;

    @BeforeEach
    public void setUp() {
        // Clear the database before each test
        budgetRepository.deleteAll();
        expenseRepository.deleteAll();
        incomeRepository.deleteAll();
    }

    @Test
    public void testGenerateSpendingTrendReport() {
        // Add test data to the database
        User user = new User();
        user.setId("testuser");
        user.setBaseCurrency("USD");
        userRepository.save(user); // Ensure the user exists

        Budget budget = new Budget();
        budget.setUserId("testuser");
        budget.setCategory("Groceries");
        budget.setLimit(1200.0);
        budgetRepository.save(budget);

        Expense expense = new Expense();
        expense.setUserId("testuser");
        expense.setCategory("Groceries");
        expense.setAmount(1000.0);
        expense.setCurrencyCode("USD");
        expense.setDate(new Date(2023 - 1900, 9, 1)); // October 1, 2023
        expenseRepository.save(expense);

        Income income = new Income();
        income.setUserId("testuser");
        income.setAmount(5000.0);
        income.setCurrencyCode("USD");
        income.setDate(new Date(2023 - 1900, 9, 1)); // October 1, 2023
        incomeRepository.save(income);

        // Mock currency conversion
        when(currencyUtil.getBaseCurrencyForUser("testuser")).thenReturn("USD");
        when(currencyConverter.convertToBaseCurrency(any(String.class), any(Double.class), any(String.class)))
                .thenAnswer(invocation -> invocation.getArgument(1)); // Return the same amount

        // Mock service methods
        when(incomeService.calculateTotalIncomeInBaseCurrency("testuser")).thenReturn(5000.0);
        when(expenseService.calculateTotalExpensesInBaseCurrency("testuser")).thenReturn(3000.0);
        when(goalsAndSavingsService.calculateNetSavings("testuser", new Date(2023 - 1900, 9, 1), new Date(2023 - 1900, 9, 31)))
                .thenReturn(2000.0);

        // Generate the report
        Map<String, Object> report = reportService.generateSpendingTrendReport("testuser", new Date(2023 - 1900, 9, 1), new Date(2023 - 1900, 9, 31));

        // Verify the report
        assertEquals("testuser", report.get("userId"));
        assertEquals(5000.0, report.get("totalIncome"));
        assertEquals(3000.0, report.get("totalExpenses"));
        assertEquals(2000.0, report.get("netSavings"));
        assertEquals("Groceries", ((Map<?, ?>) report.get("spendingTrends")).keySet().iterator().next());
    }
}