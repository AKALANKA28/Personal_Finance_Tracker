package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')") // Only authenticated users with ROLE_USER can access
    public ResponseEntity<Expense> addExpense(@RequestBody Expense expense, @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        expense.setUserId(authenticatedUserId); // Set the authenticated user's ID
        Expense newExpense = expenseService.addExpense(expense);
        return ResponseEntity.ok(newExpense);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@expenseService.isOwner(#id, authentication.principal.id)") // Ensure the user owns the expense
    public ResponseEntity<Expense> updateExpense(@PathVariable String id, @RequestBody Expense expense) {
        expense.setId(id); // Ensure the ID matches the path variable
        Expense updatedExpense = expenseService.updateExpense(expense);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@expenseService.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<Void> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')") // Users can view their own expenses; admins can view all
    public ResponseEntity<List<Expense>> getExpensesByUser(@PathVariable String userId) {
        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/user/{userId}/category/{category}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Expense>> getExpensesByUserAndCategory(
            @PathVariable String userId,
            @PathVariable String category) {
        List<Expense> expenses = expenseService.getExpensesByUserAndCategory(userId, category);
        return ResponseEntity.ok(expenses);
    }
}