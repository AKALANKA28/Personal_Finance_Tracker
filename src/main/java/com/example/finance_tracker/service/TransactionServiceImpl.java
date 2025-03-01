package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }


    @Override
    public Transaction addTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public boolean deleteTransaction(String transactionId) {
        transactionRepository.deleteById(transactionId);
        return true;
    }

    @Override
    public List<Transaction> getTransactionsByUser(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public List<Transaction> getTransactionsByCategory(String userId, String category) {
        return transactionRepository.findByUserIdAndCategory(userId, category);
    }

    @Override
    public List<Transaction> getTransactionsByTags(String userId, List<String> tags) {
        return transactionRepository.findByUserIdAndTagsIn(userId, Collections.singleton(tags));    }
}
