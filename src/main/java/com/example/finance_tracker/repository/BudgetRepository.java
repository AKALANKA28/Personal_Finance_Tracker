package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, String> {
    List<Budget> findByUserId(String userId);

}
