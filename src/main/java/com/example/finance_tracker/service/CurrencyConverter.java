package com.example.finance_tracker.service;

import com.example.finance_tracker.service.api.ExchangeRateApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CurrencyConverter {

    @Autowired
    private ExchangeRateApiClient exchangeRateApiClient;

    /**
     * Converts an amount from one currency to another using the latest exchange rates.
     */
    public double convertCurrency(String fromCurrency, String toCurrency, double amount, String baseCurrency) {
        Map<String, Double> rates = exchangeRateApiClient.getLatestExchangeRates(baseCurrency);

        Double fromRate = rates.get(fromCurrency);
        Double toRate = rates.get(toCurrency);

        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Invalid currency code");
        }

        return amount * (toRate / fromRate);
    }

    /**
     * Converts an amount to the base currency (e.g., USD).
     */
    public double convertToBaseCurrency(String currencyCode, double amount, String baseCurrency) {
        Map<String, Double> rates = exchangeRateApiClient.getLatestExchangeRates(baseCurrency);

        Double rate = rates.get(currencyCode);
        if (rate == null) {
            throw new IllegalArgumentException("Invalid currency code");
        }

        return amount / rate;
    }
}