package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Income;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends MongoRepository<Income, String> {
    List<Income> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    List<Income> findByUserId(String userId);
}