package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budget Management", description = "Endpoints for managing user budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Set a new budget", description = "Creates a new budget for the authenticated user.")
    public ResponseEntity<Budget> setBudget(
            @RequestBody Budget budget,
            @Parameter(description = "Authenticated user's ID", required = true)
            @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        budget.setUserId(authenticatedUserId);
        budget.setCurrencyCode("USD");
        Budget newBudget = budgetService.setBudget(budget);
        return ResponseEntity.ok(newBudget);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@budgetService.isOwner(#id, authentication.principal.id)")
    @Operation(summary = "Update an existing budget", description = "Updates the budget details for the given budget ID.")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable String id,
            @RequestBody Budget budget) {
        budget.setId(id);
        Budget updatedBudget = budgetService.updateBudget(budget);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@budgetService.isOwner(#id, authentication.principal.id)")
    @Operation(summary = "Delete a budget", description = "Deletes the specified budget if the authenticated user owns it.")
    public ResponseEntity<Void> deleteBudget(@PathVariable String id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get budgets by user ID", description = "Retrieves all budgets for a specific user.")
    public ResponseEntity<List<Budget>> getBudgetsByUser(@PathVariable String userId) {
        List<Budget> budgets = budgetService.getBudgetsByUser(userId);
        return ResponseEntity.ok(budgets);
    }

    @PostMapping("/user/{userId}/check-budget")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(summary = "Check if budget is exceeded", description = "Triggers a check to see if the user has exceeded their budget.")
    public ResponseEntity<Void> checkBudgetExceeded(@PathVariable String userId) {
        budgetService.checkBudgetExceeded(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/recommendations")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Provide budget adjustment recommendations", description = "Generates recommendations based on the user's budget usage.")
    public ResponseEntity<Void> provideBudgetAdjustmentRecommendations(@PathVariable String userId) {
        budgetService.provideBudgetAdjustmentRecommendations(userId);
        return ResponseEntity.ok().build();
    }
}
