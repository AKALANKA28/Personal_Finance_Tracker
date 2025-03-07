package com.example.finance_tracker.service;

import com.example.finance_tracker.service.api.ExchangeRateApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service
public class ExchangeRateService {

    private final ExchangeRateApiClient apiClient;

    public ExchangeRateService(ExchangeRateApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Autowired
    private CurrencyConverter currencyConverter;

    /**
     * Fetches the latest exchange rates from the external API for the specified base currency.
     */
    public Map<String, Double> getLatestExchangeRates(String baseCurrency) {
        Map<String, Double> rates = apiClient.getLatestExchangeRates(baseCurrency);

        // Debug log to check if the map is empty or null
        if (rates == null || rates.isEmpty()) {
            log.warn("Exchange rates are empty or null for base currency: {}", baseCurrency);
        } else {
            log.debug("Fetched exchange rates: {}", rates);
        }

        return rates;
    }

    /**
     * Converts an amount from one currency to another using the latest exchange rates.
     */
    public double convertCurrency(String fromCurrency, String toCurrency, double amount, String baseCurrency) {
        return currencyConverter.convertCurrency(fromCurrency, toCurrency, amount, baseCurrency);
    }

    /**
     * Converts an amount to the base currency (e.g., USD).
     */
    public double convertToBaseCurrency(String currencyCode, double amount, String baseCurrency) {
        return currencyConverter.convertToBaseCurrency(currencyCode, amount, baseCurrency);
    }
}