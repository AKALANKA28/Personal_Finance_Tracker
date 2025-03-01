package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Currency;
import com.example.finance_tracker.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PostMapping
    public ResponseEntity<Currency> addCurrency(@RequestBody Currency currency) {
        Currency newCurrency = currencyService.addCurrency(currency);
        return ResponseEntity.ok(newCurrency);
    }

    @PutMapping("/{currencyCode}/update-rate")
    public ResponseEntity<Void> updateExchangeRate(
            @PathVariable String currencyCode,
            @RequestParam double exchangeRate) {
        currencyService.updateExchangeRate(currencyCode, exchangeRate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/convert")
    public ResponseEntity<Double> convertCurrency(
            @RequestParam String userId,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam double amount) {
        double convertedAmount = currencyService.convertCurrency(userId, fromCurrency, toCurrency, amount);
        return ResponseEntity.ok(convertedAmount);
    }
}