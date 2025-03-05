package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @Autowired
    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')") // Only authenticated users can add income
    public ResponseEntity<Income> addIncome(@RequestBody Income income, @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        income.setUserId(authenticatedUserId); // Set the authenticated user's ID
        Income newIncome = incomeService.addIncome(income);
        return ResponseEntity.ok(newIncome);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@incomeService.isOwner(#id, authentication.principal.id) or hasRole('ROLE_ADMIN')") // Ensure the user owns the income or is an admin
    public ResponseEntity<Income> updateIncome(@PathVariable String id, @RequestBody Income income) {
        income.setId(id); // Ensure the ID matches the path variable
        Income updatedIncome = incomeService.updateIncome(income);
        return ResponseEntity.ok(updatedIncome);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@incomeService.isOwner(#id, authentication.principal.id) or hasRole('ROLE_ADMIN')") // Ensure the user owns the income or is an admin
    public ResponseEntity<Void> deleteIncome(@PathVariable String id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ROLE_ADMIN')") // Users can view their own incomes; admins can view all
    public ResponseEntity<List<Income>> getIncomesByUser(@PathVariable String userId) {
        List<Income> incomes = incomeService.getIncomesByUser(userId);
        return ResponseEntity.ok(incomes);
    }


    // Fetch all incomes for a user in their preferred currency
    @GetMapping("/user/{userId}/preferred-currency")
    public ResponseEntity<List<Income>> getIncomesByUserInPreferredCurrency(
            @PathVariable String userId,
            @RequestParam String preferredCurrency) {

        List<Income> incomes = incomeService.getIncomesByUserInPreferredCurrency(userId, preferredCurrency);
        return ResponseEntity.ok(incomes);
    }

    // Fetch a single income by ID and convert it to a preferred currency
    @GetMapping("/{incomeId}/preferred-currency")
    public ResponseEntity<Income> getIncomeInPreferredCurrency(
            @PathVariable String incomeId,
            @RequestParam String preferredCurrency) {

        Income income = incomeService.getIncomeById(incomeId);
        Income convertedIncome = incomeService.convertIncomeToPreferredCurrency(income, preferredCurrency);
        return ResponseEntity.ok(convertedIncome);
    }

    // Calculate total income for a user in the base currency
    @GetMapping("/user/{userId}/total-base-currency")
    public ResponseEntity<Double> getTotalIncomeInBaseCurrency(@PathVariable String userId) {
        double totalIncome = incomeService.calculateTotalIncomeInBaseCurrency(userId);
        return ResponseEntity.ok(totalIncome);
    }


    @GetMapping("/{id}")
    @PreAuthorize("@incomeService.isOwner(#id, authentication.principal.id) or hasRole('ROLE_ADMIN')") // Ensure the user owns the income or is an admin
    public ResponseEntity<Income> getIncomeById(@PathVariable String id) {
        Income income = incomeService.getIncomeById(id);
        return ResponseEntity.ok(income);
    }


}