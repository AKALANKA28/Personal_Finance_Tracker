package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Currency;
import com.example.finance_tracker.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Only admins can add currencies
    public ResponseEntity<Currency> addCurrency(@RequestBody Currency currency, @RequestAttribute("authenticatedUserId") String authenticatedUserId) {
        currency.setUserId(authenticatedUserId); // Set the authenticated user's ID
        Currency newCurrency = currencyService.addCurrency(currency);
        return ResponseEntity.ok(newCurrency);
    }

    @PutMapping("/{currencyCode}/update-rate")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Only admins can update exchange rates
    public ResponseEntity<Void> updateExchangeRate(
            @PathVariable String currencyCode,
            @RequestParam double exchangeRate) {
        currencyService.updateExchangeRate(currencyCode, exchangeRate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        List<Currency> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }


    @GetMapping("/convert")
    @PreAuthorize("#userId == authentication.principal.id") // Users can only convert currency for themselves
    public ResponseEntity<Double> convertCurrency(
            @RequestParam String userId,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam double amount) {
        double convertedAmount = currencyService.convertCurrency(userId, fromCurrency, toCurrency, amount);
        return ResponseEntity.ok(convertedAmount);
    }

    @GetMapping("/convert-to-base")
    public ResponseEntity<Double> convertToBaseCurrency(
            @RequestParam String currencyCode,
            @RequestParam double amount) {
        double convertedAmount = currencyService.convertToBaseCurrency(currencyCode, amount);
        return ResponseEntity.ok(convertedAmount);
    }
}