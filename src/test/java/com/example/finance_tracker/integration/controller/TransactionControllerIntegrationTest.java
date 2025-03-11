package com.example.finance_tracker.integration.controller;

import com.example.finance_tracker.TestHelper;
import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.model.User;
import com.example.finance_tracker.repository.TransactionRepository;
import com.example.finance_tracker.repository.UserRepository;
import com.example.finance_tracker.service.CurrencyConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private CurrencyConverter currencyConverter;

    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    public void setUp() {
        // Clear any existing data
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        testUser = new User();
        testUser.setId("testuser");
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setBaseCurrency("USD");
        userRepository.save(testUser);

        // Create a test transaction
        testTransaction = new Transaction();
        testTransaction.setUserId("testuser");
        testTransaction.setAmount(100.0);
        testTransaction.setCurrencyCode("USD");
        testTransaction.setType("Income");
        testTransaction.setSource("Salary");
        testTransaction.setCategory("Salary");
        testTransaction.setDate(TestHelper.parseDate("2023-10-01"));
        testTransaction.setTags(Arrays.asList("Salary", "Monthly"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testAddTransaction() throws Exception {
        String transactionJson = objectMapper.writeValueAsString(testTransaction);

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction created successfully"));

        // Verify transaction was saved
        List<Transaction> savedTransactions = transactionRepository.findByUserId("testuser");
        assertEquals(1, savedTransactions.size());
        assertEquals("Salary", savedTransactions.get(0).getSource());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testUpdateTransaction() throws Exception {
        // Save a transaction first
        Transaction savedTransaction = transactionRepository.save(testTransaction);

        // Update the transaction
        savedTransaction.setAmount(200.0);
        savedTransaction.setCategory("Bonus");
        String updatedTransactionJson = objectMapper.writeValueAsString(savedTransaction);

        mockMvc.perform(put("/api/transactions/" + savedTransaction.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTransactionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(200.0)))
                .andExpect(jsonPath("$.category", is("Bonus")));

        // Verify transaction was updated
        Transaction updated = transactionRepository.findById(savedTransaction.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(200.0, updated.getAmount());
        assertEquals("Bonus", updated.getCategory());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testDeleteTransaction() throws Exception {
        // Save a transaction first
        Transaction savedTransaction = transactionRepository.save(testTransaction);

        mockMvc.perform(delete("/api/transactions/" + savedTransaction.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify transaction was deleted
        assertFalse(transactionRepository.existsById(savedTransaction.getId()));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTransactionsByUser() throws Exception {
        // Save a transaction first
        transactionRepository.save(testTransaction);

        mockMvc.perform(get("/api/transactions/user/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].source", is("Salary")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTransactionsByCategory() throws Exception {
        // Save a transaction first
        transactionRepository.save(testTransaction);

        mockMvc.perform(get("/api/transactions/user/testuser/category/Salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("Salary")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTransactionsByTags() throws Exception {
        // Save a transaction first
        transactionRepository.save(testTransaction);

        mockMvc.perform(get("/api/transactions/user/testuser/tags")
                        .param("tags", "Salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tags[0]", is("Salary")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTransactionById() throws Exception {
        // Save a transaction first
        Transaction savedTransaction = transactionRepository.save(testTransaction);

        mockMvc.perform(get("/api/transactions/" + savedTransaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTransaction.getId())))
                .andExpect(jsonPath("$.source", is("Salary")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTransactionInPreferredCurrency() throws Exception {
        // Mock the currency converter
        when(currencyConverter.convertCurrency(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(150.0);

        // Save a transaction first
        Transaction savedTransaction = transactionRepository.save(testTransaction);

        mockMvc.perform(get("/api/transactions/" + savedTransaction.getId() + "/preferred-currency")
                        .param("preferredCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(150.0)))
                .andExpect(jsonPath("$.currencyCode", is("EUR")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testGetTransactionsByUserInPreferredCurrency() throws Exception {
        // Mock the currency converter
        when(currencyConverter.convertCurrency(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(150.0);

        // Save a transaction first
        transactionRepository.save(testTransaction);

        mockMvc.perform(get("/api/transactions/user/testuser/preferred-currency")
                        .param("preferredCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount", is(150.0)))
                .andExpect(jsonPath("$[0].currencyCode", is("EUR")));
    }

//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    public void testCreateRecurringTransaction() throws Exception {
//        testTransaction.setIsRecurring(true);
//        testTransaction.setFrequency("MONTHLY");
//        String transactionJson = objectMapper.writeValueAsString(testTransaction);
//
//        mockMvc.perform(post("/api/transactions/recurring")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(transactionJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.isRecurring", is(true)))
//                .andExpect(jsonPath("$.frequency", is("MONTHLY")));
//
//        // Verify recurring transaction was saved
//        List<Transaction> savedTransactions = transactionRepository.findByUserId("testuser");
//        assertEquals(1, savedTransactions.size());
//        assertTrue(savedTransactions.get(0).getIsRecurring());
//    }
//
//    @Test
//    @WithMockUser(username = "testuser", roles = {"USER"})
//    public void testUpdateRecurringTransaction() throws Exception {
//        // Save a recurring transaction first
//        testTransaction.setIsRecurring(true);
//        testTransaction.setFrequency("MONTHLY");
//        Transaction savedTransaction = transactionRepository.save(testTransaction);
//
//        // Update the recurring transaction
//        savedTransaction.setAmount(200.0);
//        savedTransaction.setFrequency("WEEKLY");
//        String updatedTransactionJson = objectMapper.writeValueAsString(savedTransaction);
//
//        mockMvc.perform(put("/api/transactions/recurring/" + savedTransaction.getId())
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(updatedTransactionJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.amount", is(200.0)))
//                .andExpect(jsonPath("$.frequency", is("WEEKLY")));
//
//        // Verify transaction was updated
//        Transaction updated = transactionRepository.findById(savedTransaction.getId()).orElse(null);
//        assertNotNull(updated);
//        assertEquals(200.0, updated.getAmount());
//        assertEquals("WEEKLY", updated.getFrequency());
//    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testDeleteRecurringTransaction() throws Exception {
        // Save a recurring transaction first
        testTransaction.setIsRecurring(true);
        testTransaction.setType("MONTHLY");
        Transaction savedTransaction = transactionRepository.save(testTransaction);

        mockMvc.perform(delete("/api/transactions/recurring/" + savedTransaction.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify transaction was deleted
        assertFalse(transactionRepository.existsById(savedTransaction.getId()));
    }

    @Test
    @WithMockUser(username = "wronguser", roles = {"USER"})
    public void testUnauthorizedAccessToTransaction() throws Exception {
        // Save a transaction for testuser
        Transaction savedTransaction = transactionRepository.save(testTransaction);

        // Try to access with different user
        mockMvc.perform(get("/api/transactions/" + savedTransaction.getId()))
                .andExpect(status().isForbidden());
    }
}