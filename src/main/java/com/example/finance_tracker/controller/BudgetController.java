package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<Budget> setBudget(@RequestBody Budget budget) {
        Budget newBudget = budgetService.setBudget(budget);
        return ResponseEntity.ok(newBudget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable String id, @RequestBody Budget budget) {
        budget.setId(id); // Ensure the ID matches the path variable
        Budget updatedBudget = budgetService.updateBudget(budget);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable String id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getBudgetsByUser(@PathVariable String userId) {
        List<Budget> budgets = budgetService.getBudgetsByUser(userId);
        return ResponseEntity.ok(budgets);
    }


    @PostMapping("/user/{userId}/check-budget")
    public ResponseEntity<Void> checkBudgetExceeded(@PathVariable String userId) {
        budgetService.checkBudgetExceeded(userId);
        return ResponseEntity.ok().build();
    }
}
