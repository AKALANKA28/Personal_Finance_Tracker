package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Currency;

public interface CurrencyService {
    Currency addCurrency(Currency currency);
    void updateExchangeRate(String currencyCode, double exchangeRate);
    double convertCurrency(String userId, String fromCurrency, String toCurrency, double amount);
}