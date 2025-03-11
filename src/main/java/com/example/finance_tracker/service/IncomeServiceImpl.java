package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import com.example.finance_tracker.exception.InvalidInputException;
import com.example.finance_tracker.exception.CurrencyConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("incomeService")
public class IncomeServiceImpl implements IncomeService {

    private static final Logger logger = LoggerFactory.getLogger(IncomeServiceImpl.class);

    private final IncomeRepository incomeRepository;
    private final CurrencyConverterImpl currencyConverterImpl;
    private final CurrencyUtil currencyUtil;

    @Autowired
    public IncomeServiceImpl(IncomeRepository incomeRepository, CurrencyConverterImpl currencyConverterImpl, CurrencyUtil currencyUtil) {
        this.incomeRepository = incomeRepository;
        this.currencyConverterImpl = currencyConverterImpl;
        this.currencyUtil = currencyUtil;
    }

    @Override
    public Income addIncome(Income income) {
        logger.info("Attempting to add income: {}", income);

        // Save income
        Income savedIncome = incomeRepository.save(income);
        logger.info("Income added successfully: {}", savedIncome);
        return savedIncome;
    }

    @Override
    public Income updateIncome(Income income) {
        logger.info("Attempting to update income with ID: {}", income.getId());

        // Validate input
        if (income == null || income.getId() == null) {
            logger.error("Invalid income data provided: {}", income);
            throw new InvalidInputException("Invalid income data provided");
        }

        // Check if income exists
        if (!incomeRepository.existsById(income.getId())) {
            logger.error("Income not found with ID: {}", income.getId());
            throw new ResourceNotFoundException("Income not found");
        }

        // Update income
        Income updatedIncome = incomeRepository.save(income);
        logger.info("Income updated successfully: {}", updatedIncome);
        return updatedIncome;
    }

    @Override
    public void deleteIncome(String id) {
        logger.info("Attempting to delete income with ID: {}", id);

        // Validate input
        if (id == null) {
            logger.error("Income ID cannot be null");
            throw new InvalidInputException("Income ID cannot be null");
        }

        // Check if income exists
        if (!incomeRepository.existsById(id)) {
            logger.error("Income not found with ID: {}", id);
            throw new ResourceNotFoundException("Income not found");
        }

        // Delete income
        incomeRepository.deleteById(id);
        logger.info("Income deleted successfully with ID: {}", id);
    }

    @Override
    public List<Income> getIncomesByUser(String userId) {
        logger.info("Fetching incomes for user: {}", userId);

        // Validate input
        if (userId == null) {
            logger.error("User ID cannot be null");
            throw new InvalidInputException("User ID cannot be null");
        }

        // Fetch incomes
        List<Income> incomes = incomeRepository.findByUserId(userId);
        logger.info("Found {} incomes for user: {}", incomes.size(), userId);
        return incomes;
    }

    @Override
    public List<Income> getIncomesByUserInPreferredCurrency(String userId, String preferredCurrency) {
        logger.info("Fetching incomes for user: {} in preferred currency: {}", userId, preferredCurrency);

        // Validate input
        if (userId == null || preferredCurrency == null) {
            logger.error("User ID and preferred currency cannot be null");
            throw new InvalidInputException("User ID and preferred currency cannot be null");
        }

        // Fetch incomes
        List<Income> incomes = incomeRepository.findByUserId(userId);
        logger.info("Found {} incomes for user: {}", incomes.size(), userId);

        // Convert incomes to preferred currency
        List<Income> convertedIncomes = incomes.stream()
                .map(income -> convertIncomeToPreferredCurrency(income, preferredCurrency))
                .collect(Collectors.toList());
        logger.info("Converted {} incomes to preferred currency: {}", convertedIncomes.size(), preferredCurrency);
        return convertedIncomes;
    }

    @Override
    public Income convertIncomeToPreferredCurrency(Income income, String preferredCurrency) {
        logger.info("Converting income to preferred currency: {}", preferredCurrency);

        // Validate input
        if (income == null || preferredCurrency == null) {
            logger.error("Income and preferred currency cannot be null");
            throw new InvalidInputException("Income and preferred currency cannot be null");
        }

        // Fetch base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(income.getUserId());
        logger.debug("Base currency for user: {}", baseCurrency);

        try {
            // Convert amount
            double convertedAmount = currencyConverterImpl.convertCurrency(income.getCurrencyCode(), preferredCurrency, income.getAmount(), baseCurrency);
            logger.debug("Converted amount: {} {} to {} {}", income.getAmount(), income.getCurrencyCode(), convertedAmount, preferredCurrency);

            // Create converted income
            Income convertedIncome = new Income();
            convertedIncome.setId(income.getId());
            convertedIncome.setUserId(income.getUserId());
            convertedIncome.setAmount(convertedAmount);
            convertedIncome.setCurrencyCode(preferredCurrency);
            convertedIncome.setSource(income.getSource());
            convertedIncome.setDate(income.getDate());

            logger.info("Income converted successfully: {}", convertedIncome);
            return convertedIncome;
        } catch (Exception e) {
            logger.error("Failed to convert currency for income: {}", income, e);
            throw new CurrencyConversionException("Failed to convert currency", e);
        }
    }

    @Override
    public Income getIncomeById(String id) {
        logger.info("Fetching income with ID: {}", id);

        // Validate input
        if (id == null) {
            logger.error("Income ID cannot be null");
            throw new InvalidInputException("Income ID cannot be null");
        }

        // Fetch income
        return incomeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Income not found with ID: {}", id);
                    return new ResourceNotFoundException("Income not found");
                });
    }

    @Override
    public boolean isOwner(String incomeId, String userId) {
        logger.info("Checking if user: {} is the owner of income: {}", userId, incomeId);

        // Validate input
        if (incomeId == null || userId == null) {
            logger.error("Income ID and User ID cannot be null");
            throw new InvalidInputException("Income ID and User ID cannot be null");
        }

        // Fetch income
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> {
                    logger.error("Income not found with ID: {}", incomeId);
                    return new ResourceNotFoundException("Income not found");
                });

        // Check ownership
        boolean isOwner = income.getUserId().equals(userId);
        logger.info("User: {} is owner of income: {}: {}", userId, incomeId, isOwner);
        return isOwner;
    }

    @Override
    public double calculateTotalIncomeInBaseCurrency(String userId) {
        logger.info("Calculating total income in base currency for user: {}", userId);

        // Validate input
        if (userId == null) {
            logger.error("User ID cannot be null");
            throw new InvalidInputException("User ID cannot be null");
        }

        // Fetch incomes
        List<Income> incomes = incomeRepository.findByUserId(userId);
        logger.info("Found {} incomes for user: {}", incomes.size(), userId);

        // Fetch base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);
        logger.debug("Base currency for user: {}", baseCurrency);

        // Calculate total income
        double totalIncome = incomes.stream()
                .mapToDouble(income -> {
                    try {
                        return currencyConverterImpl.convertToBaseCurrency(income.getCurrencyCode(), income.getAmount(), baseCurrency);
                    } catch (Exception e) {
                        logger.error("Failed to convert currency for income: {}", income, e);
                        throw new CurrencyConversionException("Failed to convert currency", e);
                    }
                })
                .sum();

        logger.info("Total income in base currency: {} for user: {}", totalIncome, userId);
        return totalIncome;
    }
}