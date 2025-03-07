package com.example.finance_tracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ExchangeRateResponse {
    private String result;
    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;
}
