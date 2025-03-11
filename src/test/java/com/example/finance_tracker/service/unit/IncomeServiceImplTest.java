package com.example.finance_tracker.service.unit;

import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.service.CurrencyConverterImpl;
import com.example.finance_tracker.service.IncomeServiceImpl;
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

class IncomeServiceImplTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private CurrencyConverterImpl currencyConverterImpl;

    @Mock
    private CurrencyUtil currencyUtil;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addIncome_Success() {
        // Arrange
        Income income = new Income();
        income.setUserId("user123");
        income.setAmount(1000.0);
        income.setCurrencyCode("USD");

        when(incomeRepository.save(income)).thenReturn(income);

        // Act
        Income result = incomeService.addIncome(income);

        // Assert
        assertNotNull(result);
        verify(incomeRepository, times(1)).save(income);
    }



    @Test
    void updateIncome_Success() {
        // Arrange
        Income income = new Income();
        income.setId("123");
        income.setUserId("user123");

        when(incomeRepository.existsById("123")).thenReturn(true);
        when(incomeRepository.save(income)).thenReturn(income);

        // Act
        Income result = incomeService.updateIncome(income);

        // Assert
        assertNotNull(result);
        verify(incomeRepository, times(1)).save(income);
    }

    @Test
    void updateIncome_NotFound_ThrowsException() {
        // Arrange
        Income income = new Income();
        income.setId("123");

        when(incomeRepository.existsById("123")).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.updateIncome(income);
        });

        assertEquals("Income not found", exception.getMessage());
    }

    @Test
    void deleteIncome_Success() {
        // Arrange
        String incomeId = "123";

        when(incomeRepository.existsById(incomeId)).thenReturn(true);
        doNothing().when(incomeRepository).deleteById(incomeId);

        // Act
        incomeService.deleteIncome(incomeId);

        // Assert
        verify(incomeRepository, times(1)).deleteById(incomeId);
    }

    @Test
    void deleteIncome_NotFound_ThrowsException() {
        // Arrange
        String incomeId = "123";

        when(incomeRepository.existsById(incomeId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.deleteIncome(incomeId);
        });

        assertEquals("Income not found", exception.getMessage());
    }


    @Test
    void getIncomesByUser_Success() {
        // Arrange
        String userId = "user123";
        Income income = new Income();
        income.setUserId(userId);

        when(incomeRepository.findByUserId(userId)).thenReturn(Collections.singletonList(income));

        // Act
        List<Income> result = incomeService.getIncomesByUser(userId);

        // Assert
        assertEquals(1, result.size());
        verify(incomeRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getIncomesByUserInPreferredCurrency_Success() {
        // Arrange
        String userId = "user123";
        String preferredCurrency = "EUR";
        Income income = new Income();
        income.setUserId(userId);
        income.setCurrencyCode("USD");
        income.setAmount(1000.0);

        when(incomeRepository.findByUserId(userId)).thenReturn(Collections.singletonList(income));
        when(currencyConverterImpl.convertCurrency("USD", preferredCurrency, 1000.0, "LKR")).thenReturn(850.0);

        // Act
        List<Income> result = incomeService.getIncomesByUserInPreferredCurrency(userId, preferredCurrency);

        // Assert
        assertEquals(1, result.size());
        assertEquals(850.0, result.get(0).getAmount());
        assertEquals("EUR", result.get(0).getCurrencyCode());
    }

    @Test
    void convertIncomeToPreferredCurrency_Success() {
        // Arrange
        Income income = new Income();
        income.setCurrencyCode("USD");
        income.setAmount(1000.0);

        when(currencyConverterImpl.convertCurrency("USD", "EUR", 1000.0, "LKR")).thenReturn(850.0);

        // Act
        Income result = incomeService.convertIncomeToPreferredCurrency(income, "EUR");

        // Assert
        assertEquals(850.0, result.getAmount());
        assertEquals("EUR", result.getCurrencyCode());
    }
}