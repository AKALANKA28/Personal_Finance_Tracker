package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.repository.BudgetRepository;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("budgetService")
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;

    @Autowired
    public BudgetServiceImpl(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }
    @Override
    public Budget setBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public Budget updateBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public boolean deleteBudget(String budgetId) {
        budgetRepository.deleteById(budgetId);
        return true;
    }

    @Override
    public List<Budget> getBudgetsByUser(String userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    public void checkBudgetExceeded(String userId) {
        // Logic to check if the budget is exceeded and send notifications
    }

    public boolean isOwner(String budgetId, String userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));

        return budget.getUserId().equals(userId); // Check if the user owns the budget
    }

//    @Override
//    public void updateBudget(Budget updatedBudget) {
//        Budget existingBudget = getBudgetById(updatedBudget.getId());
//        existingBudget.setName(updatedBudget.getName());
//        existingBudget.setAmount(updatedBudget.getAmount());
//        existingBudget.setCurrency(updatedBudget.getCurrency());
//        budgetRepository.save(existingBudget);
//    }
}
