package com.example.finance_tracker.controller;

import com.example.finance_tracker.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
@Tag(name = "Exchange Rate Controller", description = "APIs for managing exchange rates and currency conversions")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/latest")
    @Operation(
            summary = "Get latest exchange rates",
            description = "Retrieve the latest exchange rates based on the base currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rates"),
            @ApiResponse(responseCode = "400", description = "Invalid base currency provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Double>> getLatestExchangeRates(
            @Parameter(description = "Base currency code (e.g., LKR, EUR)", required = true, example = "LKR")
            @RequestParam(defaultValue = "LKR") String baseCurrency) {
        Map<String, Double> rates = exchangeRateService.getLatestExchangeRates(baseCurrency);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/convert")
    @Operation(
            summary = "Convert currency",
            description = "Convert an amount from one currency to another using the latest exchange rates."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully converted currency"),
            @ApiResponse(responseCode = "400", description = "Invalid currency codes or amount provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Double> convertCurrency(
            @Parameter(description = "Currency code to convert from (e.g., LKR)", required = true, example = "LKR")
            @RequestParam String fromCurrency,
            @Parameter(description = "Currency code to convert to (e.g., EUR)", required = true, example = "EUR")
            @RequestParam String toCurrency,
            @Parameter(description = "Amount to convert", required = true, example = "100.0")
            @RequestParam double amount,
            @Parameter(description = "Base currency code (e.g., LKR)", required = true, example = "LKR")
            @RequestParam(defaultValue = "LKR") String baseCurrency) {
        double convertedAmount = exchangeRateService.convertCurrency(fromCurrency, toCurrency, amount, baseCurrency);
        return ResponseEntity.ok(convertedAmount);
    }

    @GetMapping("/convert-to-base")
    @Operation(
            summary = "Convert to base currency",
            description = "Convert an amount from a specified currency to the base currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully converted to base currency"),
            @ApiResponse(responseCode = "400", description = "Invalid currency code or amount provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Double> convertToBaseCurrency(
            @Parameter(description = "Currency code to convert from (e.g., EUR)", required = true, example = "EUR")
            @RequestParam String currencyCode,
            @Parameter(description = "Amount to convert", required = true, example = "100.0")
            @RequestParam double amount,
            @Parameter(description = "Base currency code (e.g., LKR)", required = true, example = "LKR")
            @RequestParam(defaultValue = "LKR") String baseCurrency) {
        double convertedAmount = exchangeRateService.convertToBaseCurrency(currencyCode, amount, baseCurrency);
        return ResponseEntity.ok(convertedAmount);
    }
}