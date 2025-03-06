package com.example.finance_tracker.service;

import com.example.finance_tracker.service.api.ExchangeRateApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ExchangeRateService {

    @Autowired
    private ExchangeRateApiClient exchangeRateApiClient;

    @Autowired
    private CurrencyConverter currencyConverter;

    /**
     * Fetches the latest exchange rates from the external API.
     */
    public Map<String, Double> getLatestExchangeRates() {
        return exchangeRateApiClient.getLatestExchangeRates();
    }

    /**
     * Converts an amount from one currency to another.
     */
    public double convertCurrency(String fromCurrency, String toCurrency, double amount) {
        return currencyConverter.convertCurrency(fromCurrency, toCurrency, amount);
    }

    /**
     * Converts an amount to the base currency (e.g., USD).
     */
    public double convertToBaseCurrency(String currencyCode, double amount) {
        return currencyConverter.convertToBaseCurrency(currencyCode, amount);
    }
}