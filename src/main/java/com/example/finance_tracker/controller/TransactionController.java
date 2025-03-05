package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @PreAuthorize("#transaction.userId == authentication.principal.id") // Ensure the user is creating a transaction for themselves
    public ResponseEntity<String> addTransaction(@RequestBody Transaction transaction) {
        Transaction newTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.ok("Transaction created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("@transactionService.isOwner(#id, authentication.principal.id)") // Ensure the user owns the transaction
    public ResponseEntity<Transaction> updateTransaction(@PathVariable String id, @RequestBody Transaction transaction) {
        transaction.setId(id); // Ensure the ID matches the path variable
        Transaction updatedTransaction = transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@transactionService.isOwner(#id, authentication.principal.id)") // Ensure the user owns the transaction
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id") // Ensure the user is viewing their own transactions
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable String userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/category/{category}")
    @PreAuthorize("#userId == authentication.principal.id") // Ensure the user is viewing their own transactions
    public ResponseEntity<List<Transaction>> getTransactionsByCategory(
            @PathVariable String userId,
            @PathVariable String category) {
        List<Transaction> transactions = transactionService.getTransactionsByCategory(userId, category);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/tags")
    @PreAuthorize("#userId == authentication.principal.id") // Ensure the user is viewing their own transactions
    public ResponseEntity<List<Transaction>> getTransactionsByTags(
            @PathVariable String userId,
            @RequestParam List<String> tags) {
        List<Transaction> transactions = transactionService.getTransactionsByTags(userId, tags);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("@transactionService.isOwner(#id, authentication.principal.id)") // Ensure the user is viewing their own transactions
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/user/{userId}/preferred-currency")
    @PreAuthorize("#userId == authentication.principal.id") // Ensure the user is viewing their own transactions
    public ResponseEntity<List<Transaction>> getTransactionsByUserInPreferredCurrency(
            @PathVariable String userId,
            @RequestParam String preferredCurrency) {

        List<Transaction> transactions = transactionService.getTransactionsByUserInPreferredCurrency(userId, preferredCurrency);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}/preferred-currency")
    @PreAuthorize("@transactionService.isOwner(#transactionId, authentication.principal.id)") // Ensure the user is viewing their own transactions
    public ResponseEntity<Transaction> getTransactionInPreferredCurrency(
            @PathVariable String transactionId,
            @RequestParam String preferredCurrency) {

        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }

        Transaction convertedTransaction = transactionService.convertTransactionToPreferredCurrency(transaction, preferredCurrency);
        return ResponseEntity.ok(convertedTransaction);
    }

}