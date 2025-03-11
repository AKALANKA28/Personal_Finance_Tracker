package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Income;

import java.util.List;

public interface IncomeService {
    Income addIncome(Income income);

    Income updateIncome(Income income);

    void deleteIncome(String id);

    List<Income> getIncomesByUser(String userId);

    List<Income> getIncomesByUserInPreferredCurrency(String userId, String preferredCurrency);

    Income convertIncomeToPreferredCurrency(Income income, String preferredCurrency);

    Income getIncomeById(String id);

    boolean isOwner(String incomeId, String userId); // Check if the user owns the income

    double calculateTotalIncomeInBaseCurrency(String userId);
}