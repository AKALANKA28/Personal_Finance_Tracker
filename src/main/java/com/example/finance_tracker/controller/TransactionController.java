package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Controller", description = "APIs for managing user transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @PreAuthorize("#transaction.userId == authentication.principal.id")
    @Operation(
            summary = "Add a new transaction",
            description = "Add a new transaction for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> addTransaction(
            @Parameter(description = "Transaction details to add", required = true)
            @RequestBody Transaction transaction) {
        Transaction newTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.ok("Transaction created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("@transactionService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Update an existing transaction",
            description = "Update a transaction by ID. Only the owner of the transaction can update it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Transaction> updateTransaction(
            @Parameter(description = "ID of the transaction to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated transaction details", required = true)
            @RequestBody Transaction transaction) {
        transaction.setId(id);
        Transaction updatedTransaction = transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@transactionService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Delete a transaction",
            description = "Delete a transaction by ID. Only the owner of the transaction can delete it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "ID of the transaction to delete", required = true)
            @PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get transactions by user ID",
            description = "Retrieve all transactions for a specific user. Users can only view their own transactions."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Transaction>> getTransactionsByUser(
            @Parameter(description = "ID of the user to retrieve transactions for", required = true)
            @PathVariable String userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/category/{category}")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get transactions by category",
            description = "Retrieve all transactions for a specific user filtered by category."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User or category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Transaction>> getTransactionsByCategory(
            @Parameter(description = "ID of the user to retrieve transactions for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Category to filter transactions by", required = true)
            @PathVariable String category) {
        List<Transaction> transactions = transactionService.getTransactionsByCategory(userId, category);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/tags")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get transactions by tags",
            description = "Retrieve all transactions for a specific user filtered by tags."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tags provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Transaction>> getTransactionsByTags(
            @Parameter(description = "ID of the user to retrieve transactions for", required = true)
            @PathVariable String userId,
            @Parameter(description = "List of tags to filter transactions by", required = true)
            @RequestParam List<String> tags) {
        List<Transaction> transactions = transactionService.getTransactionsByTags(userId, tags);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("@transactionService.isOwner(#transactionId, authentication.principal.id)")
    @Operation(
            summary = "Get a transaction by ID",
            description = "Retrieve a specific transaction by its ID. Only the owner of the transaction can access it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Transaction> getTransactionById(
            @Parameter(description = "ID of the transaction to retrieve", required = true)
            @PathVariable String transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{transactionId}/preferred-currency")
    @PreAuthorize("@transactionService.isOwner(#transactionId, authentication.principal.id)")
    @Operation(
            summary = "Get a transaction in preferred currency",
            description = "Retrieve a specific transaction by its ID and convert it to a preferred currency. Only the owner of the transaction can access it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid currency provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Transaction> getTransactionInPreferredCurrency(
            @Parameter(description = "ID of the transaction to retrieve", required = true)
            @PathVariable String transactionId,
            @Parameter(description = "Preferred currency code (e.g., USD, EUR)", required = true)
            @RequestParam String preferredCurrency) {

        Transaction transaction = transactionService.getTransactionById(transactionId);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }

        Transaction convertedTransaction = transactionService.convertTransactionToPreferredCurrency(transaction, preferredCurrency);
        return ResponseEntity.ok(convertedTransaction);
    }

    @GetMapping("/user/{userId}/preferred-currency")
    @PreAuthorize("#userId == authentication.principal.id")
    @Operation(
            summary = "Get transactions in preferred currency",
            description = "Retrieve all transactions for a specific user converted to their preferred currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid currency provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Transaction>> getTransactionsByUserInPreferredCurrency(
            @Parameter(description = "ID of the user to retrieve transactions for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Preferred currency code (e.g., USD, EUR)", required = true)
            @RequestParam String preferredCurrency) {

        List<Transaction> transactions = transactionService.getTransactionsByUserInPreferredCurrency(userId, preferredCurrency);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/recurring")
    @Operation(
            summary = "Create a recurring transaction",
            description = "Create a new recurring transaction for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Transaction> createRecurringTransaction(
            @Parameter(description = "Recurring transaction details to add", required = true)
            @RequestBody Transaction transaction) {
        transaction.setIsRecurring(true);
        Transaction savedTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.ok(savedTransaction);
    }

    @PutMapping("/recurring/{id}")
    @PreAuthorize("@transactionService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Update a recurring transaction",
            description = "Update a recurring transaction by ID. Only the owner of the transaction can update it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recurring transaction updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction data provided"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Transaction> updateRecurringTransaction(
            @Parameter(description = "ID of the recurring transaction to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated recurring transaction details", required = true)
            @RequestBody Transaction transaction) {
        transaction.setId(id);
        transaction.setIsRecurring(true);
        Transaction updatedTransaction = transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/recurring/{id}")
    @PreAuthorize("@transactionService.isOwner(#id, authentication.principal.id)")
    @Operation(
            summary = "Delete a recurring transaction",
            description = "Delete a recurring transaction by ID. Only the owner of the transaction can delete it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Recurring transaction deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteRecurringTransaction(
            @Parameter(description = "ID of the recurring transaction to delete", required = true)
            @PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}