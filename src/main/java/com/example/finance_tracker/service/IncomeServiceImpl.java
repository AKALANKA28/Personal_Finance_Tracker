package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.repository.IncomeRepository;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("incomeService")
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final CurrencyService currencyService;

    @Autowired
    public IncomeServiceImpl(IncomeRepository incomeRepository, CurrencyService currencyService) {
        this.incomeRepository = incomeRepository;
        this.currencyService = currencyService;
    }

    @Override
    public Income addIncome(Income income) {
        return incomeRepository.save(income);
    }

    @Override
    public Income updateIncome(Income income) {
        // Ensure the income exists
        if (!incomeRepository.existsById(income.getId())) {
            throw new ResourceNotFoundException("Income not found");
        }
        return incomeRepository.save(income);
    }

    @Override
    public void deleteIncome(String id) {
        if (!incomeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Income not found");
        }
        incomeRepository.deleteById(id);
    }

    @Override
    public List<Income> getIncomesByUser(String userId) {
        return incomeRepository.findByUserId(userId);
    }

    @Override
    public List<Income> getIncomesByUserInPreferredCurrency(String userId, String preferredCurrency) {
        List<Income> incomes = incomeRepository.findByUserId(userId);

        // Convert each income's amount to the preferred currency
        return incomes.stream()
                .map(income -> convertIncomeToPreferredCurrency(income, preferredCurrency))
                .collect(Collectors.toList());
    }

    @Override
    public Income convertIncomeToPreferredCurrency(Income income, String preferredCurrency) {
        String originalCurrency = income.getCurrencyCode();
        double originalAmount = income.getAmount();

        // Convert the amount to the preferred currency
        double convertedAmount = currencyService.convertCurrency(
                income.getUserId(),
                originalCurrency,
                preferredCurrency,
                originalAmount
        );

        // Create a new income object with the converted amount and preferred currency
        Income convertedIncome = new Income();
        convertedIncome.setId(income.getId());
        convertedIncome.setUserId(income.getUserId());
        convertedIncome.setAmount(convertedAmount);
        convertedIncome.setCurrencyCode(preferredCurrency);
        convertedIncome.setSource(income.getSource());
        convertedIncome.setDate(income.getDate());
//        convertedIncome.setDescription(income.getDescription());

        return convertedIncome;
    }

    @Override
    public Income getIncomeById(String id) {
        return incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));
    }

    @Override
    public boolean isOwner(String incomeId, String userId) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found"));
        return income.getUserId().equals(userId);
    }

    @Override
    public double calculateTotalIncomeInBaseCurrency(String userId) {
        List<Income> incomes = incomeRepository.findByUserId(userId);

        // Convert each income's amount to the base currency and sum them up
        return incomes.stream()
                .mapToDouble(income -> currencyService.convertToBaseCurrency(income.getCurrencyCode(), income.getAmount()))
                .sum();
    }
}