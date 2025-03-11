package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByUserIdAndCategory(String userId, String category);
    List<Expense> findByUserId(String userId);
    List<Expense> findByUserIdAndDateBetween(String userId, Date startDate, Date endDate);
    List<Expense> findByUserIdAndCategoryAndDateBetween(String userId, String category, Date startDate, Date endDate);

//    double calculateTotalExpenses();
}
