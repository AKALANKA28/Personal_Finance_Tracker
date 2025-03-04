package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service( "expenseService")
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public Expense updateExpense(Expense expense) {
        // Ensure the expense exists
        if (!expenseRepository.existsById(expense.getId())) {
            throw new ResourceNotFoundException("Expense not found");
        }
        return expenseRepository.save(expense);
    }

    @Override
    public boolean deleteExpense(String id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found");
        }
        expenseRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Expense> getExpensesByUser(String userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    public List<Expense> getExpensesByUserAndCategory(String userId, String category) {
        return expenseRepository.findByUserIdAndCategory(userId, category);
    }

    @Override
    public boolean isOwner(String expenseId, String userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        return expense.getUserId().equals(userId); // Check if the user owns the expense
    }
}