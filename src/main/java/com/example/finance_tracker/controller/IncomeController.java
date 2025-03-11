package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.service.IncomeService;
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
@RequestMapping("/api/incomes")
@Tag(name = "Income Controller", description = "APIs for managing user incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @Autowired
    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Add a new income",
            description = "Add a new income for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Income added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid income data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Income> addIncome(
            @Parameter(description = "Income details to add", required = true)
            @RequestBody Income income,
            @Parameter(description = "Authenticated user ID", required = true)
            @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        income.setUserId(authenticatedUserId);
        Income newIncome = incomeService.addIncome(income);
        return ResponseEntity.ok(newIncome);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@incomeService.isOwner(#id, authentication.principal.id) or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Update an existing income",
            description = "Update an income by ID. Only the owner of the income or an admin can update it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Income updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid income data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Income not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Income> updateIncome(
            @Parameter(description = "ID of the income to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated income details", required = true)
            @RequestBody Income income) {
        income.setId(id);
        Income updatedIncome = incomeService.updateIncome(income);
        return ResponseEntity.ok(updatedIncome);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@incomeService.isOwner(#id, authentication.principal.id) or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Delete an income",
            description = "Delete an income by ID. Only the owner of the income or an admin can delete it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Income deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Income not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteIncome(
            @Parameter(description = "ID of the income to delete", required = true)
            @PathVariable String id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get incomes by user ID",
            description = "Retrieve all incomes for a specific user. Users can only view their own incomes unless they are admins."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incomes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Income>> getIncomesByUser(
            @Parameter(description = "ID of the user to retrieve incomes for", required = true)
            @PathVariable String userId) {
        List<Income> incomes = incomeService.getIncomesByUser(userId);
        return ResponseEntity.ok(incomes);
    }

    @GetMapping("/user/{userId}/preferred-currency")
    @Operation(
            summary = "Get incomes in preferred currency",
            description = "Retrieve all incomes for a specific user converted to their preferred currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incomes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid currency provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Income>> getIncomesByUserInPreferredCurrency(
            @Parameter(description = "ID of the user to retrieve incomes for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Preferred currency code (e.g., USD, EUR)", required = true)
            @RequestParam String preferredCurrency) {
        List<Income> incomes = incomeService.getIncomesByUserInPreferredCurrency(userId, preferredCurrency);
        return ResponseEntity.ok(incomes);
    }

    @GetMapping("/{incomeId}/preferred-currency")
    @Operation(
            summary = "Get an income in preferred currency",
            description = "Retrieve a single income by ID and convert it to a preferred currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Income retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid currency provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Income not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Income> getIncomeInPreferredCurrency(
            @Parameter(description = "ID of the income to retrieve", required = true)
            @PathVariable String incomeId,
            @Parameter(description = "Preferred currency code (e.g., USD, EUR)", required = true)
            @RequestParam String preferredCurrency) {
        Income income = incomeService.getIncomeById(incomeId);
        Income convertedIncome = incomeService.convertIncomeToPreferredCurrency(income, preferredCurrency);
        return ResponseEntity.ok(convertedIncome);
    }

    @GetMapping("/user/{userId}/total-base-currency")
    @Operation(
            summary = "Calculate total income in base currency",
            description = "Calculate the total income for a specific user in the base currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total income calculated successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Double> getTotalIncomeInBaseCurrency(
            @Parameter(description = "ID of the user to calculate total income for", required = true)
            @PathVariable String userId) {
        double totalIncome = incomeService.calculateTotalIncomeInBaseCurrency(userId);
        return ResponseEntity.ok(totalIncome);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@incomeService.isOwner(#id, authentication.principal.id) or hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Get an income by ID",
            description = "Retrieve a specific income by its ID. Only the owner of the income or an admin can access it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Income retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Income not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Income> getIncomeById(
            @Parameter(description = "ID of the income to retrieve", required = true)
            @PathVariable String id) {
        Income income = incomeService.getIncomeById(id);
        return ResponseEntity.ok(income);
    }
}