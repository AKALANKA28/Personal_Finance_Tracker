package com.example.finance_tracker.integration.controller;

import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.service.IncomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class IncomeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private IncomeService incomeService;

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
    @WithMockUser(username = "user123", roles = {"USER"})
    void addIncome_Success() throws Exception {
        // Arrange
        when(incomeService.addIncome(any(Income.class))).thenReturn(income);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user123\",\"amount\":1000.0,\"currencyCode\":\"USD\",\"source\":\"Salary\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.source").value("Salary"));

        // Verify
        verify(incomeService, times(1)).addIncome(any(Income.class));
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void updateIncome_Success() throws Exception {
        // Arrange
        when(incomeService.updateIncome(any(Income.class))).thenReturn(income);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/api/incomes/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"123\",\"userId\":\"user123\",\"amount\":1000.0,\"currencyCode\":\"USD\",\"source\":\"Salary\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.source").value("Salary"));

        // Verify
        verify(incomeService, times(1)).updateIncome(any(Income.class));
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void deleteIncome_Success() throws Exception {
        // Arrange
        doNothing().when(incomeService).deleteIncome("123");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/incomes/123"))
                .andExpect(status().isNoContent());

        // Verify
        verify(incomeService, times(1)).deleteIncome("123");
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getIncomesByUser_Success() throws Exception {
        // Arrange
        when(incomeService.getIncomesByUser("user123")).thenReturn(Collections.singletonList(income));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/incomes/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].amount").value(1000.0))
                .andExpect(jsonPath("$[0].currencyCode").value("USD"))
                .andExpect(jsonPath("$[0].source").value("Salary"));

        // Verify
        verify(incomeService, times(1)).getIncomesByUser("user123");
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getIncomesByUserInPreferredCurrency_Success() throws Exception {
        // Arrange
        when(incomeService.getIncomesByUserInPreferredCurrency("user123", "EUR")).thenReturn(Collections.singletonList(income));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/incomes/user/user123/preferred-currency")
                        .param("preferredCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].amount").value(1000.0))
                .andExpect(jsonPath("$[0].currencyCode").value("USD"))
                .andExpect(jsonPath("$[0].source").value("Salary"));

        // Verify
        verify(incomeService, times(1)).getIncomesByUserInPreferredCurrency("user123", "EUR");
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getIncomeInPreferredCurrency_Success() throws Exception {
        // Arrange
        when(incomeService.getIncomeById("123")).thenReturn(income);
        when(incomeService.convertIncomeToPreferredCurrency(income, "EUR")).thenReturn(income);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/incomes/123/preferred-currency")
                        .param("preferredCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.source").value("Salary"));

        // Verify
        verify(incomeService, times(1)).getIncomeById("123");
        verify(incomeService, times(1)).convertIncomeToPreferredCurrency(income, "EUR");
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getTotalIncomeInBaseCurrency_Success() throws Exception {
        // Arrange
        when(incomeService.calculateTotalIncomeInBaseCurrency("user123")).thenReturn(200000.0);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/incomes/user/user123/total-base-currency"))
                .andExpect(status().isOk())
                .andExpect(content().string("200000.0"));

        // Verify
        verify(incomeService, times(1)).calculateTotalIncomeInBaseCurrency("user123");
    }

    @Test
    @WithMockUser(username = "user123", roles = {"USER"})
    void getIncomeById_Success() throws Exception {
        // Arrange
        when(incomeService.getIncomeById("123")).thenReturn(income);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/incomes/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.source").value("Salary"));

        // Verify
        verify(incomeService, times(1)).getIncomeById("123");
    }
}