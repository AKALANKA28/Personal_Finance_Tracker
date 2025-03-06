package com.example.finance_tracker.controller;

import com.example.finance_tracker.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Double>> getLatestExchangeRates() {
        Map<String, Double> rates = exchangeRateService.getLatestExchangeRates();
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/convert")
    public ResponseEntity<Double> convertCurrency(
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam double amount) {
        double convertedAmount = exchangeRateService.convertCurrency(fromCurrency, toCurrency, amount);
        return ResponseEntity.ok(convertedAmount);
    }

    @GetMapping("/convert-to-base")
    public ResponseEntity<Double> convertToBaseCurrency(
            @RequestParam String currencyCode,
            @RequestParam double amount) {
        double convertedAmount = exchangeRateService.convertToBaseCurrency(currencyCode, amount);
        return ResponseEntity.ok(convertedAmount);
    }
}