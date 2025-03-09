package com.example.finance_tracker.service.intergration;

import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.service.CurrencyConverterImpl;
import com.example.finance_tracker.service.IncomeServiceImpl;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class IncomeServiceImplIntegrationTest {

    @Autowired
    private IncomeServiceImpl incomeService;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private CurrencyConverterImpl currencyConverterImpl;

    @Mock
    private CurrencyUtil currencyUtil;

    private Income income;

    @BeforeEach
    void setUp() {
        income = new Income();
        income.setId("123");
        income.setUserId("user123");
        income.setAmount(1000.0);
        income.setCurrencyCode("USD");
        income.setSource("Salary");
    }

    @Test
    void addIncome_Success() {
        // Arrange
        when(incomeRepository.save(any(Income.class))).thenReturn(income);

        // Act
        Income result = incomeService.addIncome(income);

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals(1000.0, result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
        assertEquals("Salary", result.getSource());

        // Verify
        verify(incomeRepository, times(1)).save(income);
    }

    @Test
    void updateIncome_Success() {
        // Arrange
        when(incomeRepository.existsById("123")).thenReturn(true);
        when(incomeRepository.save(any(Income.class))).thenReturn(income);

        // Act
        Income result = incomeService.updateIncome(income);

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals(1000.0, result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
        assertEquals("Salary", result.getSource());

        // Verify
        verify(incomeRepository, times(1)).existsById("123");
        verify(incomeRepository, times(1)).save(income);
    }

    @Test
    void updateIncome_NotFound_ThrowsException() {
        // Arrange
        when(incomeRepository.existsById("123")).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.updateIncome(income);
        });

        assertEquals("Income not found", exception.getMessage());

        // Verify
        verify(incomeRepository, times(1)).existsById("123");
    }

    @Test
    void deleteIncome_Success() {
        // Arrange
        when(incomeRepository.existsById("123")).thenReturn(true);
        doNothing().when(incomeRepository).deleteById("123");

        // Act
        incomeService.deleteIncome("123");

        // Verify
        verify(incomeRepository, times(1)).existsById("123");
        verify(incomeRepository, times(1)).deleteById("123");
    }

    @Test
    void deleteIncome_NotFound_ThrowsException() {
        // Arrange
        when(incomeRepository.existsById("123")).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.deleteIncome("123");
        });

        assertEquals("Income not found", exception.getMessage());

        // Verify
        verify(incomeRepository, times(1)).existsById("123");
    }

    @Test
    void getIncomesByUser_Success() {
        // Arrange
        when(incomeRepository.findByUserId("user123")).thenReturn(Collections.singletonList(income));

        // Act
        List<Income> result = incomeService.getIncomesByUser("user123");

        // Assert
        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getId());
        assertEquals("user123", result.get(0).getUserId());
        assertEquals(1000.0, result.get(0).getAmount());
        assertEquals("USD", result.get(0).getCurrencyCode());
        assertEquals("Salary", result.get(0).getSource());

        // Verify
        verify(incomeRepository, times(1)).findByUserId("user123");
    }

    @Test
    void getIncomesByUserInPreferredCurrency_Success() {
        // Arrange
        when(incomeRepository.findByUserId("user123")).thenReturn(Collections.singletonList(income));
        when(currencyUtil.getBaseCurrencyForUser("user123")).thenReturn("LKR");
        when(currencyConverterImpl.convertCurrency("USD", "EUR", 1000.0, "LKR")).thenReturn(850.0);

        // Act
        List<Income> result = incomeService.getIncomesByUserInPreferredCurrency("user123", "EUR");

        // Assert
        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getId());
        assertEquals("user123", result.get(0).getUserId());
        assertEquals(850.0, result.get(0).getAmount());
        assertEquals("EUR", result.get(0).getCurrencyCode());
        assertEquals("Salary", result.get(0).getSource());

        // Verify
        verify(incomeRepository, times(1)).findByUserId("user123");
        verify(currencyUtil, times(1)).getBaseCurrencyForUser("user123");
        verify(currencyConverterImpl, times(1)).convertCurrency("USD", "EUR", 1000.0, "LKR");
    }

    @Test
    void convertIncomeToPreferredCurrency_Success() {
        // Arrange
        when(currencyUtil.getBaseCurrencyForUser("user123")).thenReturn("LKR");
        when(currencyConverterImpl.convertCurrency("USD", "EUR", 1000.0, "LKR")).thenReturn(850.0);

        // Act
        Income result = incomeService.convertIncomeToPreferredCurrency(income, "EUR");

        // Assert
        assertEquals("123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals(850.0, result.getAmount());
        assertEquals("EUR", result.getCurrencyCode());
        assertEquals("Salary", result.getSource());

        // Verify
        verify(currencyUtil, times(1)).getBaseCurrencyForUser("user123");
        verify(currencyConverterImpl, times(1)).convertCurrency("USD", "EUR", 1000.0, "LKR");
    }

    @Test
    void getIncomeById_Success() {
        // Arrange
        when(incomeRepository.findById("123")).thenReturn(Optional.of(income));

        // Act
        Income result = incomeService.getIncomeById("123");

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals(1000.0, result.getAmount());
        assertEquals("USD", result.getCurrencyCode());
        assertEquals("Salary", result.getSource());

        // Verify
        verify(incomeRepository, times(1)).findById("123");
    }

    @Test
    void getIncomeById_NotFound_ThrowsException() {
        // Arrange
        when(incomeRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.getIncomeById("123");
        });

        assertEquals("Income not found", exception.getMessage());

        // Verify
        verify(incomeRepository, times(1)).findById("123");
    }

    @Test
    void isOwner_Success() {
        // Arrange
        when(incomeRepository.findById("123")).thenReturn(Optional.of(income));

        // Act
        boolean result = incomeService.isOwner("123", "user123");

        // Assert
        assertTrue(result);

        // Verify
        verify(incomeRepository, times(1)).findById("123");
    }

    @Test
    void isOwner_NotFound_ThrowsException() {
        // Arrange
        when(incomeRepository.findById("123")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.isOwner("123", "user123");
        });

        assertEquals("Income not found", exception.getMessage());

        // Verify
        verify(incomeRepository, times(1)).findById("123");
    }

    @Test
    void calculateTotalIncomeInBaseCurrency_Success() {
        // Arrange
        when(incomeRepository.findByUserId("user123")).thenReturn(Collections.singletonList(income));
        when(currencyUtil.getBaseCurrencyForUser("user123")).thenReturn("LKR");
        when(currencyConverterImpl.convertToBaseCurrency("USD", 1000.0, "LKR")).thenReturn(200000.0);

        // Act
        double result = incomeService.calculateTotalIncomeInBaseCurrency("user123");

        // Assert
        assertEquals(200000.0, result);

        // Verify
        verify(incomeRepository, times(1)).findByUserId("user123");
        verify(currencyUtil, times(1)).getBaseCurrencyForUser("user123");
        verify(currencyConverterImpl, times(1)).convertToBaseCurrency("USD", 1000.0, "LKR");
    }
}