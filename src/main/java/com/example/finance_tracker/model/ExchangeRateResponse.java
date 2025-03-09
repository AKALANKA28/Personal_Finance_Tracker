package com.example.finance_tracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ExchangeRateResponse {
    private String result; // e.g., "success"
    @JsonProperty("base_code")
    private String baseCurrency; // e.g., "USD"
    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates; // e.g., {"EUR": 1.5, "GBP": 1.2}

    // Constructor to initialize all fields
    public ExchangeRateResponse(String result, String baseCurrency, Map<String, Double> conversionRates) {
        this.result = result;
        this.baseCurrency = baseCurrency;
        this.conversionRates = conversionRates;
    }

    // Default constructor (required for deserialization by Jackson)
    public ExchangeRateResponse() {
    }
}