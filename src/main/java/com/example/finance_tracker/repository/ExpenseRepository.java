package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
//    List<Expense> findByUserIdAndCategory(String userId, String category);
    List<Expense> findByUserId(String userId);
    List<Expense> findByUserIdAndDateBetween(String userId, Date startDate, Date endDate);
    List<Expense> findByUserIdAndCategoryAndDateBetween(String userId, String category, Date startDate, Date endDate);

    // Find all expenses for a specific user and category
    @Query("{ 'userId': ?0, 'category': { $regex: ?1, $options: 'i' } }") // Case-insensitive regex
    List<Expense> findByUserIdAndCategory(String userId, String category);

    // Calculate total spending for a specific user and category
    @Query(value = "{ 'userId': ?0, 'category': { $regex: ?1, $options: 'i' } }", fields = "{ 'amount': 1 }")
    List<Expense> findAmountsByUserIdAndCategory(String userId, String category);

    // Calculate total spending for a specific user across all categories
    @Query(value = "{ 'userId': ?0 }", fields = "{ 'amount': 1 }")
    List<Expense> findAmountsByUserId(String userId);
}
