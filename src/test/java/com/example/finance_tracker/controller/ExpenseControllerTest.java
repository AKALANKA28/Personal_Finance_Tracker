//package com.example.finance_tracker.ControllerTest;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import com.example.finance_tracker.controller.ExpenseController;
//import com.example.finance_tracker.model.Expense;
//import com.example.finance_tracker.service.ExpenseService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(ExpenseController.class)
//public class ExpenseControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ExpenseService expenseService;
//
//    @Test
//    @WithMockUser(roles = "USER")
//    void testAddExpense() throws Exception {
//        Expense expense = new Expense();
//        expense.setCategory("Food");
//        expense.setAmount(50.0);
//
//        when(expenseService.addExpense(any(Expense.class))).thenReturn(expense);
//
//        mockMvc.perform(post("/api/expenses")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"category\":\"Food\",\"amount\":50.0}"))
//                .andExpect(status().isOk());
//
//        verify(expenseService, times(1)).addExpense(any(Expense.class));
//    }
//}