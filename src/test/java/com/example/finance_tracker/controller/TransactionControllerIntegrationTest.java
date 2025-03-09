package com.example.finance_tracker.controller;

import com.example.finance_tracker.TestHelper;
import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.repository.TransactionRepository;
import com.example.finance_tracker.service.CurrencyConverterImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use the test profile
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Mock
    private CurrencyConverterImpl currencyConverterImpl;

    @BeforeEach
    public void setup() {
        transactionRepository.deleteAll(); // Clear the database before each test
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testAddTransaction() throws Exception {
        String transactionJson = """
            {
                "userId": "testuser",
                "amount": 100.0,
                "currencyCode": "USD",
                "type": "Income",
                "source": "Salary",
                "date": "2023-10-01"
            }
            """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Transaction created successfully"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testUpdateTransaction() throws Exception {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01")); // Use the helper method
        transactionRepository.save(transaction);

        String updatedTransactionJson = """
            {
                "id": "%s",
                "userId": "testuser",
                "amount": 200.0,
                "currencyCode": "USD",
                "type": "Income",
                "source": "Bonus",
                "date": "2023-10-01"
            }
            """.formatted(transaction.getId());

        mockMvc.perform(put("/api/transactions/{id}", transaction.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTransactionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("Bonus"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testDeleteTransaction() throws Exception {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        mockMvc.perform(delete("/api/transactions/{id}", transaction.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testGetTransactionsByUser() throws Exception {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        mockMvc.perform(get("/api/transactions/user/{userId}", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source").value("Salary"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testGetTransactionsByCategory() throws Exception {
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

        mockMvc.perform(get("/api/transactions/user/{userId}/category/{category}", "testuser", "Salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Salary"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testGetTransactionsByTags() throws Exception {
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

        mockMvc.perform(get("/api/transactions/user/{userId}/tags?tags=Salary", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tags[0]").value("Salary"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testGetTransactionById() throws Exception {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        mockMvc.perform(get("/api/transactions/{transactionId}", transaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("Salary"));
    }



    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    public void testGetTransactionsByUserInPreferredCurrency() throws Exception {
        // Add a transaction first
        Transaction transaction = new Transaction();
        transaction.setUserId("testuser");
        transaction.setAmount(100.0);
        transaction.setCurrencyCode("USD");
        transaction.setType("Income");
        transaction.setSource("Salary");
        transaction.setDate(TestHelper.parseDate("2023-10-01"));
        transactionRepository.save(transaction);

        // Mock the currency converter in the service layer
        when(currencyConverterImpl.convertCurrency(any(), any(), any(), any())).thenReturn(150.0);

        mockMvc.perform(get("/api/transactions/user/{userId}/preferred-currency", "testuser")
                        .param("preferredCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(150.0))
                .andExpect(jsonPath("$[0].currencyCode").value("EUR"));
    }



}