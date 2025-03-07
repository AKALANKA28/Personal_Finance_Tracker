package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("expenseService")
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CurrencyConverter currencyConverter;
    private final CurrencyUtil currencyUtil;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CurrencyConverter currencyConverter, CurrencyUtil currencyUtil) {
        this.expenseRepository = expenseRepository;
        this.currencyConverter = currencyConverter;
        this.currencyUtil = currencyUtil;
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
    public List<Expense> getExpensesByUserInPreferredCurrency(String userId, String preferredCurrency) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        // Convert each expense's amount to the preferred currency
        return expenses.stream()
                .map(expense -> convertExpenseToPreferredCurrency(expense, preferredCurrency))
                .collect(Collectors.toList());
    }

    @Override
    public Expense convertExpenseToPreferredCurrency(Expense expense, String preferredCurrency) {
        String originalCurrency = expense.getCurrencyCode();
        double originalAmount = expense.getAmount();

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(expense.getUserId());

        // Convert the amount to the preferred currency
        double convertedAmount = currencyConverter.convertCurrency(originalCurrency, preferredCurrency, originalAmount, baseCurrency);


        // Create a new expense object with the converted amount and preferred currency
        Expense convertedExpense = new Expense();
        convertedExpense.setId(expense.getId());
        convertedExpense.setUserId(expense.getUserId());
        convertedExpense.setAmount(convertedAmount);
        convertedExpense.setCurrencyCode(preferredCurrency);
        convertedExpense.setCategory(expense.getCategory());
        convertedExpense.setDate(expense.getDate());
        convertedExpense.setDescription(expense.getDescription());

        return convertedExpense;
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

    @Override
    public double calculateTotalExpensesInBaseCurrency(String userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);

        // Convert each expense's amount to the base currency and sum them up
        return expenses.stream()
                .mapToDouble(expense -> currencyConverter.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount(),  baseCurrency))
                .sum();
    }

    @Override
    public Expense getExpenseById(String expenseId) {
        return null;
    }
}