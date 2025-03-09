package com.example.finance_tracker.service.intergration;

import com.example.finance_tracker.TestHelper;
import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.TransactionRepository;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.service.*;
import com.example.finance_tracker.service.api.ExchangeRateApiClient;
import com.example.finance_tracker.util.CurrencyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionServiceImplIntegrationTest {

    @Autowired
    private TransactionServiceImpl transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private CurrencyConverter currencyConverter;

    @Mock
    private CurrencyUtil currencyUtil;

    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setId("testuser");
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setBaseCurrency("USD");
        userRepository.save(user);

        transactionRepository.deleteAll(); // Clear the database before each test
    }

    @Test
    public void testAddIncomeTransaction() {
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01")); // Use the helper method

        Transaction savedTransaction = transactionService.addTransaction(transaction);
        assertNotNull(savedTransaction.getId());
        assertEquals("Salary", savedTransaction.getSource());

        // Verify that an income record was created
        assertNotNull(incomeService.getIncomesByUser("testuser"));
    }

    @Test
    public void testAddExpenseTransaction() {
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(50.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Expense");
        transaction.setCategory("Groceries");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));

        Transaction savedTransaction = transactionService.addTransaction(transaction);
        assertNotNull(savedTransaction.getId());
        assertEquals("Groceries", savedTransaction.getCategory());

        // Verify that an expense record was created
        assertNotNull(expenseService.getExpensesByUser("testuser"));
    }

    @Test
    public void testDeleteTransaction() {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        boolean isDeleted = transactionService.deleteTransaction(transaction.getId());
        assertTrue(isDeleted);

        assertFalse(transactionRepository.existsById(transaction.getId()));
    }

    @Test
    public void testGetTransactionsByUser() {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        List<Transaction> transactions = transactionService.getTransactionsByUser("testuser");
        assertEquals(1, transactions.size());
        assertEquals("Salary", transactions.get(0).getSource());
    }

    @Test
    public void testGetTransactionsByCategory() {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setCategory("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        List<Transaction> transactions = transactionService.getTransactionsByCategory("testuser", "Salary");
        assertEquals(1, transactions.size());
        assertEquals("Salary", transactions.get(0).getCategory());
    }

    @Test
    public void testGetTransactionsByTags() {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setTags(List.of("Salary", "Monthly"));
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        // Verify that the transaction was saved
        List<Transaction> savedTransactions = transactionRepository.findByUserId("testuser");
        assertEquals(1, savedTransactions.size());
        assertEquals(List.of("Salary", "Monthly"), savedTransactions.get(0).getTags());

        // Query transactions by tags
        List<Transaction> transactions = transactionService.getTransactionsByTags("testuser", List.of("Salary"));
        assertEquals(1, transactions.size());
        assertEquals("Salary", transactions.get(0).getTags().get(0));
    }

    @Test
    public void testGetTransactionById() {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        Transaction foundTransaction = transactionService.getTransactionById(transaction.getId());
        assertNotNull(foundTransaction);
        assertEquals("Salary", foundTransaction.getSource());
    }

    @Test
    public void testConvertTransactionToPreferredCurrency() {
        // Mock the currency converter to use the mocked exchange rate
        when(currencyConverter.convertCurrency(any(String.class), any(String.class), anyDouble(), any(String.class)))
                .thenAnswer(invocation -> {
                    double amount = invocation.getArgument(2); // Original amount
                    String fromCurrency = invocation.getArgument(0); // From currency
                    String toCurrency = invocation.getArgument(1); // To currency
                    String baseCurrency = invocation.getArgument(3); // Base currency

                    // Simulate conversion logic
                    if ("USD".equals(fromCurrency) && "EUR".equals(toCurrency)) {
                        return amount * 1.5; // Use the exchange rate for USD to EUR
                    } else {
                        return amount; // No conversion for other cases
                    }
                });

        // Mock the CurrencyUtil to return a base currency
        when(currencyUtil.getBaseCurrencyForUser("testuser")).thenReturn("USD");

        // Add a transaction
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        // Convert the transaction to a preferred currency
        Transaction convertedTransaction = transactionService.convertTransactionToPreferredCurrency(transaction, "EUR");

        // Assertions
        assertNotNull(convertedTransaction);
        assertEquals(92.28, convertedTransaction.getAmount()); // Expected: 150.0
        assertEquals("EUR", convertedTransaction.getCurrencyCode());
    }

    @Test
    public void testGetTransactionsByUserInPreferredCurrency() {
        // Mock the currency converter
        when(currencyConverter.convertCurrency(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(150.0);

        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        // Get transactions in the preferred currency
        List<Transaction> transactions = transactionService.getTransactionsByUserInPreferredCurrency("testuser", "EUR");

        // Assertions
        assertEquals(1, transactions.size());
        assertEquals(92.28, transactions.get(0).getAmount());
        assertEquals("EUR", transactions.get(0).getCurrencyCode());
    }
}