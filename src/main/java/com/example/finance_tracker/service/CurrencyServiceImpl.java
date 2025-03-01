package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Currency;
import com.example.finance_tracker.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Override
    public Currency addCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }

    @Override
    public void updateExchangeRate(String currencyCode, double exchangeRate) {
        Currency currency = currencyRepository.findByCurrencyCode(currencyCode);
        currency.setExchangeRate(exchangeRate);
        currencyRepository.save(currency);
    }

    @Override
    public double convertCurrency(String userId, String fromCurrency, String toCurrency, double amount) {
        // Logic to convert currency
        return amount * 1.2; // Replace with actual conversion logic
    }

}