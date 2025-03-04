package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Currency;
import com.example.finance_tracker.repository.CurrencyRepository;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    @Transactional
    public Currency addCurrency(Currency currency) {
        // Check if the currency already exists
        currencyRepository.findByCurrencyCode(currency.getCurrencyCode())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Currency with code " + c.getCurrencyCode() + " already exists");
                });
        return currencyRepository.save(currency);
    }

    @Override
    @Transactional
    public void updateExchangeRate(String currencyCode, double exchangeRate) {
        Currency currency = currencyRepository.findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with code: " + currencyCode));
        currency.setExchangeRate(exchangeRate);
        currencyRepository.save(currency);
    }



    @Override
    @Transactional(readOnly = true)
    public double convertCurrency(String userId, String fromCurrency, String toCurrency, double amount) {
        // Fetch exchange rates for the currencies
        Currency from = currencyRepository.findByCurrencyCode(fromCurrency)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + fromCurrency));
        Currency to = currencyRepository.findByCurrencyCode(toCurrency)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + toCurrency));

        // Validate the amount
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        // Convert the amount
        return amount * (to.getExchangeRate() / from.getExchangeRate());
    }

}