package com.example.finance_tracker.repository;

import com.example.finance_tracker.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUserId(String userId);

    List<Transaction> findByUserIdAndCategory(String userId, String category);

    List<Transaction> findByUserIdAndTagsIn(String userId, Collection<List<String>> tags);
}
