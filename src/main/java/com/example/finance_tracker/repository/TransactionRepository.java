package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUserId(String userId);
    List<Transaction> findByUserIdAndCategory(String userId, String category);
    List<Transaction> findByUserIdAndTagsIn(String userId, List<String> tags);
    List<Transaction> findByIsRecurring(boolean isRecurring);

    // Fetch recent transactions for a user (sorted by date in descending order)
    @Query(value = "{ 'userId': ?0 }", sort = "{ 'date': -1 }")
    List<Transaction> findRecentTransactionsByUser(String userId, int limit);

    // Fetch all income transactions (for admin)
    @Query(value = "{ 'type': 'Income' }", fields = "{ 'amount': 1 }")
    List<Transaction> findAllIncomeTransactions();

    // Fetch all expense transactions (for admin)
    @Query(value = "{ 'type': 'Expense' }", fields = "{ 'amount': 1 }")
    List<Transaction> findAllExpenseTransactions();

    // Fetch income transactions for a specific user
    @Query(value = "{ 'userId': ?0, 'type': 'Income' }", fields = "{ 'amount': 1 }")
    List<Transaction> findIncomeTransactionsByUser(String userId);

    // Fetch expense transactions for a specific user
    @Query(value = "{ 'userId': ?0, 'type': 'Expense' }", fields = "{ 'amount': 1 }")
    List<Transaction> findExpenseTransactionsByUser(String userId);
}
