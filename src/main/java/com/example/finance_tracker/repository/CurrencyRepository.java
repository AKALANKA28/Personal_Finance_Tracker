package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Currency;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CurrencyRepository extends MongoRepository<Currency, String> {
    Optional<Currency> findByCurrencyCode(String currencyCode);
}
