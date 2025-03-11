package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.ExpenseRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.exception.CurrencyConversionException;
import com.example.finance_tracker.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("expenseService")
public class ExpenseServiceImpl implements ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseServiceImpl.class);

    private final ExpenseRepository expenseRepository;
    private final CurrencyConverterImpl currencyConverterImpl;
    private final CurrencyUtil currencyUtil;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CurrencyConverterImpl currencyConverterImpl, CurrencyUtil currencyUtil) {
        this.expenseRepository = expenseRepository;
        this.currencyConverterImpl = currencyConverterImpl;
        this.currencyUtil = currencyUtil;
    }

    @Override
    public Expense addExpense(Expense expense) {
        logger.info("Attempting to add expense: {}", expense);

        // Set default values for optional fields if they are null
        if (expense.getDescription() == null) {
            expense.setDescription("");
        }
        if (expense.getTags() == null) {
            expense.setTags(new ArrayList<>());
        }
        if (expense.getRecurrencePattern() == null) {
            expense.setRecurrencePattern("");
        }

        Expense savedExpense = expenseRepository.save(expense);
        logger.info("Expense added successfully: {}", savedExpense);
        return savedExpense;
    }

    @Override
    public Expense updateExpense(Expense expense) {
        logger.info("Attempting to update expense: {}", expense);
        if (expense == null || expense.getId() == null) {
            logger.error("Invalid expense data provided: {}", expense);
            throw new InvalidInputException("Invalid expense data provided");
        }
        if (!expenseRepository.existsById(expense.getId())) {
            logger.error("Expense not found with ID: {}", expense.getId());
            throw new ResourceNotFoundException("Expense not found");
        }
        Expense updatedExpense = expenseRepository.save(expense);
        logger.info("Expense updated successfully: {}", updatedExpense);
        return updatedExpense;
    }

    @Override
    public boolean deleteExpense(String id) {
        logger.info("Attempting to delete expense with ID: {}", id);
        if (id == null) {
            logger.error("Expense ID cannot be null");
            throw new InvalidInputException("Expense ID cannot be null");
        }
        if (!expenseRepository.existsById(id)) {
            logger.error("Expense not found with ID: {}", id);
            throw new ResourceNotFoundException("Expense not found");
        }
        expenseRepository.deleteById(id);
        logger.info("Expense deleted successfully with ID: {}", id);
        return true;
    }

    @Override
    public List<Expense> getExpensesByUser(String userId) {
        logger.info("Fetching expenses for user with ID: {}", userId);
        if (userId == null) {
            logger.error("User ID cannot be null");
            throw new InvalidInputException("User ID cannot be null");
        }
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        logger.info("Found {} expenses for user with ID: {}", expenses.size(), userId);
        return expenses;
    }

    @Override
    public List<Expense> getExpensesByUserInPreferredCurrency(String userId, String preferredCurrency) {
        logger.info("Fetching expenses for user with ID: {} in preferred currency: {}", userId, preferredCurrency);
        if (userId == null || preferredCurrency == null) {
            logger.error("User ID and preferred currency cannot be null");
            throw new InvalidInputException("User ID and preferred currency cannot be null");
        }
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        logger.info("Found {} expenses for user with ID: {}", expenses.size(), userId);

        // Convert each expense's amount to the preferred currency
        List<Expense> convertedExpenses = expenses.stream()
                .map(expense -> convertExpenseToPreferredCurrency(expense, preferredCurrency))
                .collect(Collectors.toList());
        logger.info("Converted {} expenses to preferred currency: {}", convertedExpenses.size(), preferredCurrency);
        return convertedExpenses;
    }

    @Override
    public Expense convertExpenseToPreferredCurrency(Expense expense, String preferredCurrency) {
        logger.info("Converting expense to preferred currency: {}", preferredCurrency);
        if (expense == null || preferredCurrency == null) {
            logger.error("Expense and preferred currency cannot be null");
            throw new InvalidInputException("Expense and preferred currency cannot be null");
        }
        String originalCurrency = expense.getCurrencyCode();
        double originalAmount = expense.getAmount();

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(expense.getUserId());
        logger.debug("Base currency for user: {}", baseCurrency);

        try {
            // Convert the amount to the preferred currency
            double convertedAmount = currencyConverterImpl.convertCurrency(originalCurrency, preferredCurrency, originalAmount, baseCurrency);
            logger.debug("Converted amount: {} {} to {} {}", originalAmount, originalCurrency, convertedAmount, preferredCurrency);

            // Create a new expense object with the converted amount and preferred currency
            Expense convertedExpense = new Expense();
            convertedExpense.setId(expense.getId());
            convertedExpense.setUserId(expense.getUserId());
            convertedExpense.setAmount(convertedAmount);
            convertedExpense.setCurrencyCode(preferredCurrency);
            convertedExpense.setCategory(expense.getCategory());
            convertedExpense.setDate(expense.getDate());
            convertedExpense.setDescription(expense.getDescription());

            logger.info("Expense converted successfully: {}", convertedExpense);
            return convertedExpense;
        } catch (Exception e) {
            logger.error("Failed to convert currency for expense: {}", expense, e);
            throw new CurrencyConversionException("Failed to convert currency", e);
        }
    }

    @Override
    public List<Expense> getExpensesByUserAndCategory(String userId, String category) {
        logger.info("Fetching expenses for user with ID: {} and category: {}", userId, category);
        if (userId == null || category == null) {
            logger.error("User ID and category cannot be null");
            throw new InvalidInputException("User ID and category cannot be null");
        }
        List<Expense> expenses = expenseRepository.findByUserIdAndCategory(userId, category);
        logger.info("Found {} expenses for user with ID: {} and category: {}", expenses.size(), userId, category);
        return expenses;
    }

    @Override
    public boolean isOwner(String expenseId, String userId) {
        logger.info("Checking if user with ID: {} is the owner of expense with ID: {}", userId, expenseId);
        if (expenseId == null || userId == null) {
            logger.error("Expense ID and User ID cannot be null");
            throw new InvalidInputException("Expense ID and User ID cannot be null");
        }
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> {
                    logger.error("Expense not found with ID: {}", expenseId);
                    return new ResourceNotFoundException("Expense not found");
                });
        boolean isOwner = expense.getUserId().equals(userId);
        logger.info("User with ID: {} is owner of expense with ID: {}: {}", userId, expenseId, isOwner);
        return isOwner;
    }

    @Override
    public double calculateTotalExpensesInBaseCurrency(String userId) {
        logger.info("Calculating total expenses in base currency for user with ID: {}", userId);
        if (userId == null) {
            logger.error("User ID cannot be null");
            throw new InvalidInputException("User ID cannot be null");
        }
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        logger.info("Found {} expenses for user with ID: {}", expenses.size(), userId);

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);
        logger.debug("Base currency for user: {}", baseCurrency);

        // Convert each expense's amount to the base currency and sum them up
        double total = expenses.stream()
                .mapToDouble(expense -> {
                    try {
                        return currencyConverterImpl.convertToBaseCurrency(expense.getCurrencyCode(), expense.getAmount(), baseCurrency);
                    } catch (Exception e) {
                        logger.error("Failed to convert currency for expense: {}", expense, e);
                        throw new CurrencyConversionException("Failed to convert currency", e);
                    }
                })
                .sum();
        logger.info("Total expenses in base currency for user with ID: {}: {}", userId, total);
        return total;
    }

    @Override
    public Expense getExpenseById(String expenseId) {
        logger.info("Fetching expense with ID: {}", expenseId);
        if (expenseId == null) {
            logger.error("Expense ID cannot be null");
            throw new InvalidInputException("Expense ID cannot be null");
        }
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> {
                    logger.error("Expense not found with ID: {}", expenseId);
                    return new ResourceNotFoundException("Expense not found");
                });
    }
}