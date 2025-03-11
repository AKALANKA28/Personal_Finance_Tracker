package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expense Controller", description = "APIs for managing user expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Add a new expense",
            description = "Add a new expense for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid expense data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Expense> addExpense(
            @Parameter(description = "Expense details to add", required = true)
            @RequestBody Expense expense,
            @Parameter(description = "Authenticated user ID", required = true)
            @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        expense.setUserId(authenticatedUserId);
        Expense newExpense = expenseService.addExpense(expense);
        return ResponseEntity.ok(newExpense);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@expenseService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Update an existing expense",
            description = "Update an expense by ID. Only the owner of the expense can update it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid expense data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Expense> updateExpense(
            @Parameter(description = "ID of the expense to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated expense details", required = true)
            @RequestBody Expense expense) {
        expense.setId(id);
        Expense updatedExpense = expenseService.updateExpense(expense);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@expenseService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Delete an expense",
            description = "Delete an expense by ID. Only the owner of the expense can delete it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "ID of the expense to delete", required = true)
            @PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get expenses by user ID",
            description = "Retrieve all expenses for a specific user. Users can only view their own expenses unless they are admins."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Expense>> getExpensesByUser(
            @Parameter(description = "ID of the user to retrieve expenses for", required = true)
            @PathVariable String userId) {
        List<Expense> expenses = expenseService.getExpensesByUser(userId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/user/{userId}/category/{category}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get expenses by user ID and category",
            description = "Retrieve all expenses for a specific user filtered by category."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User or category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Expense>> getExpensesByUserAndCategory(
            @Parameter(description = "ID of the user to retrieve expenses for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Category to filter expenses by", required = true)
            @PathVariable String category) {
        List<Expense> expenses = expenseService.getExpensesByUserAndCategory(userId, category);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/user/{userId}/preferred-currency")
    @Operation(
            summary = "Get expenses in preferred currency",
            description = "Retrieve all expenses for a specific user converted to their preferred currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid currency provided"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Expense>> getExpensesByUserInPreferredCurrency(
            @Parameter(description = "ID of the user to retrieve expenses for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Preferred currency code (e.g., USD, EUR)", required = true)
            @RequestParam String preferredCurrency) {
        List<Expense> expenses = expenseService.getExpensesByUserInPreferredCurrency(userId, preferredCurrency);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{expenseId}/preferred-currency")
    @Operation(
            summary = "Get an expense in preferred currency",
            description = "Retrieve a single expense by ID and convert it to a preferred currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid currency provided"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Expense> getExpenseInPreferredCurrency(
            @Parameter(description = "ID of the expense to retrieve", required = true)
            @PathVariable String expenseId,
            @Parameter(description = "Preferred currency code (e.g., USD, EUR)", required = true)
            @RequestParam String preferredCurrency) {
        Expense expense = expenseService.getExpenseById(expenseId);
        Expense convertedExpense = expenseService.convertExpenseToPreferredCurrency(expense, preferredCurrency);
        return ResponseEntity.ok(convertedExpense);
    }

    @GetMapping("/user/{userId}/total-base-currency")
    @Operation(
            summary = "Calculate total expenses in base currency",
            description = "Calculate the total expenses for a specific user in the base currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total expenses calculated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Double> getTotalExpensesInBaseCurrency(
            @Parameter(description = "ID of the user to calculate expenses for", required = true)
            @PathVariable String userId) {
        double totalExpenses = expenseService.calculateTotalExpensesInBaseCurrency(userId);
        return ResponseEntity.ok(totalExpenses);
    }

    @GetMapping("/{expenseId}")
    @Operation(
            summary = "Get an expense by ID",
            description = "Retrieve a single expense by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Expense> getExpenseById(
            @Parameter(description = "ID of the expense to retrieve", required = true)
            @PathVariable String expenseId) {
        Expense expense = expenseService.getExpenseById(expenseId);
        return ResponseEntity.ok(expense);
    }
}