package com.example.finance_tracker.service.api;

import java.util.Map;

public interface ExchangeRateApiClient {
    Map<String, Double> getLatestExchangeRates(String baseCurrency);
}