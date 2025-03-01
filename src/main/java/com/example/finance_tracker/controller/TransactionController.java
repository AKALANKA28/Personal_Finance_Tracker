package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        Transaction newTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.ok(newTransaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable String id, @RequestBody Transaction transaction) {
        transaction.setId(id); // Ensure the ID matches the path variable
        Transaction updatedTransaction = transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable String userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<List<Transaction>> getTransactionsByCategory(@PathVariable String userId, @PathVariable String category) {
        List<Transaction> transactions = transactionService.getTransactionsByCategory(userId, category);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/tags")
    public ResponseEntity<List<Transaction>> getTransactionsByTags(@PathVariable String userId, @RequestParam List<String> tags) {
        List<Transaction> transactions = transactionService.getTransactionsByTags(userId, tags);
        return ResponseEntity.ok(transactions);
    }

//    @GetMapping("/user/{userId}/recurring")
//    public ResponseEntity<List<Transaction>> getRecurringTransactions(@PathVariable String userId) {
//        List<Transaction> transactions = transactionService.getRecurringTransactions(userId);
//        return ResponseEntity.ok(transactions);
//    }
}