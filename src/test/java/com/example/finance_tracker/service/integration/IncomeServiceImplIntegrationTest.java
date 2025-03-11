package com.example.finance_tracker.service.integration;

import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.service.CurrencyConverterImpl;
import com.example.finance_tracker.service.IncomeServiceImpl;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Reset the database after each test
public class IncomeServiceImplIntegrationTest {

    @Autowired
    private IncomeRepository incomeRepository;

    @Mock
    private CurrencyConverterImpl currencyConverterImpl;

    @Mock
    private CurrencyUtil currencyUtil;

    @Autowired
    private IncomeServiceImpl incomeService;

    private Income income;

    private static final Logger logger = LoggerFactory.getLogger(IncomeServiceImplIntegrationTest.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        income = new Income();
        income.setUserId("user123");
        income.setAmount(1000.0);
        income.setCurrencyCode("LKR");
        income.setSource("Salary");
    }

    @Test
    void addIncome_ShouldSaveIncome() {
        // Act
        Income savedIncome = incomeService.addIncome(income);

        // Assert
        assertNotNull(savedIncome.getId());
        assertEquals("user123", savedIncome.getUserId());
        assertEquals(1000.0, savedIncome.getAmount());
        assertEquals("LKR", savedIncome.getCurrencyCode());
        assertEquals("Salary", savedIncome.getSource());

        // Verify by fetching from the database
        Income fetchedIncome = incomeRepository.findById(savedIncome.getId()).orElse(null);
        assertNotNull(fetchedIncome);
        assertEquals(savedIncome.getId(), fetchedIncome.getId());
    }

    @Test
    void updateIncome_ShouldUpdateExistingIncome() {
        // Arrange
        Income savedIncome = incomeRepository.save(income);

        // Update the income
        savedIncome.setAmount(1500.0);
        savedIncome.setSource("Bonus");

        // Act
        Income updatedIncome = incomeService.updateIncome(savedIncome);

        // Assert
        assertNotNull(updatedIncome);
        assertEquals(savedIncome.getId(), updatedIncome.getId());
        assertEquals(1500.0, updatedIncome.getAmount());
        assertEquals("Bonus", updatedIncome.getSource());

        // Verify by fetching from the database
        Income fetchedIncome = incomeRepository.findById(savedIncome.getId()).orElse(null);
        assertNotNull(fetchedIncome);
        assertEquals(1500.0, fetchedIncome.getAmount());
        assertEquals("Bonus", fetchedIncome.getSource());
    }

    @Test
    void updateIncome_ShouldThrowResourceNotFoundException_WhenIncomeNotFound() {
        // Arrange
        income.setId("nonExistentId");

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.updateIncome(income);
        });

        assertEquals("Income not found", exception.getMessage());
    }

    @Test
    void deleteIncome_ShouldDeleteIncome() {
        // Arrange
        Income savedIncome = incomeRepository.save(income);

        // Act
        incomeService.deleteIncome(savedIncome.getId());

        // Assert
        assertFalse(incomeRepository.existsById(savedIncome.getId()));
    }

    @Test
    void deleteIncome_ShouldThrowResourceNotFoundException_WhenIncomeNotFound() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.deleteIncome("nonExistentId");
        });

        assertEquals("Income not found", exception.getMessage());
    }

    @Test
    void getIncomesByUser_ShouldReturnUserIncomes() {
        // Arrange
        incomeRepository.save(income);

        // Act
        List<Income> incomes = incomeService.getIncomesByUser("user123");

        // Assert
//        assertEquals("2", incomes.size());
        assertEquals("user123", incomes.get(0).getUserId());
        assertEquals(1000.0, incomes.get(0).getAmount());
        assertEquals("LKR", incomes.get(0).getCurrencyCode());
        assertEquals("Salary", incomes.get(0).getSource());
    }

    @Test
    void getIncomesByUserInPreferredCurrency_ShouldReturnConvertedIncomes() {
        // Arrange
        incomeRepository.save(income);
        when(currencyUtil.getBaseCurrencyForUser("user123")).thenReturn("LKR");
        when(currencyConverterImpl.convertCurrency("LKR", "EUR", 1000.0, "LKR")).thenReturn(850.0);

        // Act
        List<Income> convertedIncomes = incomeService.getIncomesByUserInPreferredCurrency("user123", "EUR");

        // Assert
        assertEquals(1, convertedIncomes.size());
        assertEquals(850.0, convertedIncomes.get(0).getAmount());
        assertEquals("EUR", convertedIncomes.get(0).getCurrencyCode());
    }

    @Test
    void getIncomeById_ShouldReturnIncome() {
        // Arrange
        Income savedIncome = incomeRepository.save(income);

        // Act
        Income result = incomeService.getIncomeById(savedIncome.getId());

        // Assert
        assertNotNull(result);
        assertEquals(savedIncome.getId(), result.getId());
        assertEquals("user123", result.getUserId());
        assertEquals(1000.0, result.getAmount());
        assertEquals("LKR", result.getCurrencyCode());
        assertEquals("Salary", result.getSource());
    }

    @Test
    void getIncomeById_ShouldThrowResourceNotFoundException_WhenIncomeNotFound() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.getIncomeById("nonExistentId");
        });

        assertEquals("Income not found", exception.getMessage());
    }

    @Test
    void isOwner_ShouldReturnTrue_WhenUserIsOwner() {
        // Arrange
        Income savedIncome = incomeRepository.save(income);

        // Act
        boolean isOwner = incomeService.isOwner(savedIncome.getId(), "user123");

        // Assert
        assertTrue(isOwner);
    }

    @Test
    void isOwner_ShouldThrowResourceNotFoundException_WhenIncomeNotFound() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            incomeService.isOwner("nonExistentId", "user123");
        });

        assertEquals("Income not found", exception.getMessage());
    }

    @Test
    void calculateTotalIncomeInBaseCurrency_ShouldReturnTotalIncome() {
        // Arrange
        incomeRepository.save(income);
        when(currencyUtil.getBaseCurrencyForUser("user123")).thenReturn("LKR");
        when(currencyConverterImpl.convertToBaseCurrency("LKR", 1000.0, "LKR")).thenReturn(200000.0);

        // Act
        double totalIncome = incomeService.calculateTotalIncomeInBaseCurrency("user123");

        // Assert
        assertEquals(200000.0, totalIncome);
    }
}