package com.example.finance_tracker.unit;

import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.TransactionRepository;
import com.example.finance_tracker.service.CurrencyConverterImpl;
import com.example.finance_tracker.service.ExpenseService;
import com.example.finance_tracker.service.IncomeService;
import com.example.finance_tracker.service.TransactionServiceImpl;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrencyConverterImpl currencyConverterImpl;

    @Mock
    private IncomeService incomeService;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private CurrencyUtil currencyUtil;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addTransaction_Income_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setType("Income");
        transaction.setUserId("user123");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");

        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        Transaction result = transactionService.addTransaction(transaction);

        // Assert
        assertNotNull(result);
        verify(incomeService, times(1)).addIncome(any(Income.class));
        verify(expenseService, never()).addExpense(any(Expense.class));
    }


    @Test
    void addTransaction_Expense_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setType("Expense");
        transaction.setUserId("user123");
        transaction.setAmount(50.0);
        transaction.setCurrencyCode("USD");

        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        Transaction result = transactionService.addTransaction(transaction);

        // Assert
        assertNotNull(result);
        verify(expenseService, times(1)).addExpense(any(Expense.class));
        verify(incomeService, never()).addIncome(any(Income.class));
    }

    @Test
    void addTransaction_InvalidType_ThrowsException() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setType("InvalidType");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.addTransaction(transaction);
        });

        assertEquals("Invalid transaction type. Must be 'Income' or 'Expense'.", exception.getMessage());
    }


    @Test
    void updateTransaction_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId("123");
        transaction.setUserId("user123");

        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Act
        Transaction result = transactionService.updateTransaction(transaction);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void deleteTransaction_Success() {
        // Arrange
        String transactionId = "123";

        doNothing().when(transactionRepository).deleteById(transactionId);

        // Act
        boolean result = transactionService.deleteTransaction(transactionId);

        // Assert
        assertTrue(result);
        verify(transactionRepository, times(1)).deleteById(transactionId);
    }


    @Test
    void getTransactionsByUser_Success() {
        // Arrange
        String userId = "user123";
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);

        when(transactionRepository.findByUserId(userId)).thenReturn(Collections.singletonList(transaction));

        // Act
        List<Transaction> result = transactionService.getTransactionsByUser(userId);

        // Assert
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getTransactionsByCategory_Success() {
        // Arrange
        String userId = "user123";
        String category = "Food";
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setCategory(category);

        when(transactionRepository.findByUserIdAndCategory(userId, category))
                .thenReturn(Collections.singletonList(transaction));

        // Act
        List<Transaction> result = transactionService.getTransactionsByCategory(userId, category);

        // Assert
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findByUserIdAndCategory(userId, category);
    }



    @Test
    void getTransactionsByTags_Success() {
        // Arrange
        String userId = "user123";
        List<String> tags = Collections.singletonList("Groceries");
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTags(tags);

        when(transactionRepository.findByUserIdAndTagsIn(userId, tags))
                .thenReturn(Collections.singletonList(transaction));

        // Act
        List<Transaction> result = transactionService.getTransactionsByTags(userId, tags);

        // Assert
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findByUserIdAndTagsIn(userId, tags);
    }

    @Test
    void getTransactionById_Success() {
        // Arrange
        String transactionId = "123";
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        Transaction result = transactionService.getTransactionById(transactionId);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).findById(transactionId);
    }



    @Test
    void getTransactionById_NotFound() {
        // Arrange
        String transactionId = "123";

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act
        Transaction result = transactionService.getTransactionById(transactionId);

        // Assert
        assertNull(result);
    }



    @Test
    void isOwner_Success() {
        // Arrange
        String transactionId = "123";
        String userId = "user123";
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setUserId(userId);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        boolean result = transactionService.isOwner(transactionId, userId);

        // Assert
        assertTrue(result);
    }

    @Test
    void isOwner_TransactionNotFound_ThrowsException() {
        // Arrange
        String transactionId = "123";
        String userId = "user123";

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.isOwner(transactionId, userId);
        });

        assertEquals("Transaction not found", exception.getMessage());
    }


    @Test
    void getTransactionsByUserInPreferredCurrency_Success() {
        // Arrange
        String userId = "user123";
        String preferredCurrency = "EUR";
        String baseCurrency = "USD"; // Base currency for the user

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setCurrencyCode("USD"); // Original currency
        transaction.setAmount(100.0); // Original amount

        // Mock repository to return the transaction
        when(transactionRepository.findByUserId(userId)).thenReturn(Collections.singletonList(transaction));

        // Mock currencyUtil to return the base currency
        when(currencyUtil.getBaseCurrencyForUser(userId)).thenReturn(baseCurrency);

        // Mock currencyConverter to return the converted amount
        when(currencyConverterImpl.convertCurrency("USD", preferredCurrency, 100.0, baseCurrency))
                .thenReturn(85.0); // Expected converted amount

        // Act
        List<Transaction> result = transactionService.getTransactionsByUserInPreferredCurrency(userId, preferredCurrency);

        // Assert
        assertEquals(1, result.size()); // Ensure the list has one transaction
        assertEquals(85.0, result.get(0).getAmount()); // Verify the converted amount
        assertEquals("EUR", result.get(0).getCurrencyCode()); // Verify the new currency code

        // Verify interactions
        verify(transactionRepository, times(1)).findByUserId(userId);
        verify(currencyUtil, times(1)).getBaseCurrencyForUser(userId);
        verify(currencyConverterImpl, times(1)).convertCurrency("USD", preferredCurrency, 100.0, baseCurrency);
    }
}