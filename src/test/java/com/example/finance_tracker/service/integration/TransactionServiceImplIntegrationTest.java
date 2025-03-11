package com.example.finance_tracker.service.integration;

import com.example.finance_tracker.TestHelper;
import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.TransactionRepository;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.service.*;
import com.example.finance_tracker.util.CurrencyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TransactionServiceImplIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Mock
    private CurrencyConverter currencyConverter;

    @Mock
    private CurrencyUtil currencyUtil;

    @Autowired
    private TransactionServiceImpl transactionService;

    @BeforeEach
    public void setUp() {
        // Clean collections before each test
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(Transaction.class);

        // Create and save test user
        User user = new User();
        user.setId("testuser");
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setBaseCurrency("USD");
        mongoTemplate.save(user);
    }

    @Test
    public void testAddIncomeTransaction() {
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));

        Transaction savedTransaction = transactionService.addTransaction(transaction);
        assertNotNull(savedTransaction.getId());
        assertEquals("Salary", savedTransaction.getSource());

        // Verify transaction was saved using MongoTemplate
        Query query = new Query(Criteria.where("userId").is("testuser").and("source").is("Salary"));
        Transaction found = mongoTemplate.findOne(query, Transaction.class);
        assertNotNull(found);
        assertEquals(100.0, found.getAmount());
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

        // Verify transaction was saved using MongoTemplate
        Query query = new Query(Criteria.where("userId").is("testuser").and("category").is("Groceries"));
        Transaction found = mongoTemplate.findOne(query, Transaction.class);
        assertNotNull(found);
        assertEquals(50.0, found.getAmount());
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
        mongoTemplate.save(transaction);

        boolean isDeleted = transactionService.deleteTransaction(transaction.getId());
        assertTrue(isDeleted);

        // Verify using MongoTemplate
        Query query = new Query(Criteria.where("_id").is(transaction.getId()));
        Transaction found = mongoTemplate.findOne(query, Transaction.class);
        assertNull(found);
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
        mongoTemplate.save(transaction);

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
        mongoTemplate.save(transaction);

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
        mongoTemplate.save(transaction);

        // Verify that the transaction was saved
        Query query = new Query(Criteria.where("userId").is("testuser"));
        List<Transaction> savedTransactions = mongoTemplate.find(query, Transaction.class);
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
        mongoTemplate.save(transaction);

        Transaction foundTransaction = transactionService.getTransactionById(transaction.getId());
        assertNotNull(foundTransaction);
        assertEquals("Salary", foundTransaction.getSource());
    }

    @Test
    public void testConvertTransactionToPreferredCurrency() {
        // Mock the currency converter
        when(currencyConverter.convertCurrency(anyString(), anyString(), anyDouble(), anyString()))
                .thenAnswer(invocation -> {
                    double amount = invocation.getArgument(2); // Original amount
                    String fromCurrency = invocation.getArgument(0); // From currency
                    String toCurrency = invocation.getArgument(1); // To currency

                    // Simulate conversion logic
                    if ("USD".equals(fromCurrency) && "EUR".equals(toCurrency)) {
                        return amount * 0.92; // Use realistic exchange rate for USD to EUR
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
        mongoTemplate.save(transaction);

        // Convert the transaction to a preferred currency
        Transaction convertedTransaction = transactionService.convertTransactionToPreferredCurrency(transaction, "EUR");

        // Assertions
        assertNotNull(convertedTransaction);
        assertEquals(92.25, convertedTransaction.getAmount(), 0.01); // Using 0.92 rate
        assertEquals("EUR", convertedTransaction.getCurrencyCode());
    }

    @Test
    public void testGetTransactionsByUserInPreferredCurrency() {
        // Mock the currency converter
        when(currencyConverter.convertCurrency(anyString(), anyString(), anyDouble(), anyString()))
                .thenAnswer(invocation -> {
                    double amount = invocation.getArgument(2); // Original amount
                    String toCurrency = invocation.getArgument(1); // To currency

                    // Return converted amount for EUR
                    if ("EUR".equals(toCurrency)) {
                        return amount * 0.92; // USD to EUR conversion
                    }
                    return amount;
                });

        // Mock the base currency
        when(currencyUtil.getBaseCurrencyForUser("testuser")).thenReturn("USD");

        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        mongoTemplate.save(transaction);

        // Get transactions in the preferred currency
        List<Transaction> transactions = transactionService.getTransactionsByUserInPreferredCurrency("testuser", "EUR");

        // Assertions
        assertEquals(1, transactions.size());
        assertEquals(92.25, transactions.get(0).getAmount(), 0.01);
        assertEquals("EUR", transactions.get(0).getCurrencyCode());
    }
}