package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Currency;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CurrencyRepository extends MongoRepository<Currency, String> {
    Currency findByCurrencyCode(String currencyCode);
}
