package com.example.finance_tracker.controller;

import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @Operation(
            summary = "Set a new budget",
            description = "Creates a new budget for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid budget data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Budget> setBudget(
            @Parameter(description = "Budget details to create", required = true)
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
    @Operation(
            summary = "Update an existing budget",
            description = "Updates the budget details for the given budget ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid budget data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Budget not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Budget> updateBudget(
            @Parameter(description = "ID of the budget to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated budget details", required = true)
            @RequestBody Budget budget) {
        budget.setId(id);
        Budget updatedBudget = budgetService.updateBudget(budget);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@budgetService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Delete a budget",
            description = "Deletes the specified budget if the authenticated user owns it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Budget not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteBudget(
            @Parameter(description = "ID of the budget to delete", required = true)
            @PathVariable String id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get budgets by user ID",
            description = "Retrieves all budgets for a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budgets retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Budget>> getBudgetsByUser(
            @Parameter(description = "ID of the user to retrieve budgets for", required = true)
            @PathVariable String userId) {
        List<Budget> budgets = budgetService.getBudgetsByUser(userId);
        return ResponseEntity.ok(budgets);
    }

    @PostMapping("/user/{userId}/check-budget")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Check if budget is exceeded",
            description = "Triggers a check to see if the user has exceeded their budget."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget check completed successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> checkBudgetExceeded(
            @Parameter(description = "ID of the user to check budget for", required = true)
            @PathVariable String userId) {
        budgetService.checkBudgetExceeded(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/recommendations")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Provide budget adjustment recommendations",
            description = "Generates recommendations based on the user's budget usage."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommendations generated successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> provideBudgetAdjustmentRecommendations(
            @Parameter(description = "ID of the user to generate recommendations for", required = true)
            @PathVariable String userId) {
        budgetService.provideBudgetAdjustmentRecommendations(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/allocate-to-goal")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Allocate budget to a goal",
            description = "Allocate a specified amount from the user's budget to a financial goal."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget allocated to goal successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or allocation exceeds budget limit"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Budget or goal not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> allocateBudgetToGoal(
            @Parameter(description = "ID of the user", required = true)
            @RequestParam String userId,
            @Parameter(description = "ID of the goal to allocate budget to", required = true)
            @RequestParam String goalId,
            @Parameter(description = "Amount to allocate", required = true)
            @RequestParam double amount) {
        try {
            budgetService.allocateBudgetToGoal(userId, goalId, amount);
            return ResponseEntity.ok().body("Budget allocated to goal successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while allocating budget to goal.");
        }
    }
}