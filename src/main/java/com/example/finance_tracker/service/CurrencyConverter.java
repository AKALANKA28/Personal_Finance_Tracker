package com.example.finance_tracker.service;

import java.util.Map;

public interface CurrencyConverter {
    double convertCurrency(String fromCurrency, String toCurrency, double amount, String baseCurrency);
    double convertToBaseCurrency(String currencyCode, double amount, String baseCurrency);
}