package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.TransactionRepository;
import com.example.finance_tracker.util.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service("transactionService")
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CurrencyConverter currencyConverter;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, CurrencyConverter currencyConverter,
                                  IncomeService incomeService, ExpenseService expenseService) {
        this.transactionRepository = transactionRepository;
        this.currencyConverter = currencyConverter;
        this.incomeService = incomeService;
        this.expenseService = expenseService;
    }

    @Override
    public Transaction addTransaction(Transaction transaction) {
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Automatically create an income or expense record based on the transaction type
        if ("Income".equalsIgnoreCase(transaction.getType())) {
            Income income = new Income();
            income.setUserId(transaction.getUserId());
            income.setAmount(transaction.getAmount());
            income.setCurrencyCode(transaction.getCurrencyCode());
            income.setSource(transaction.getSource());
            income.setDate(transaction.getDate());
            incomeService.addIncome(income);
        } else if ("Expense".equalsIgnoreCase(transaction.getType())) {
            Expense expense = getExpense(transaction);

            expenseService.addExpense(expense);
        } else {
            throw new IllegalArgumentException("Invalid transaction type. Must be 'Income' or 'Expense'.");
        }

        return savedTransaction;
    }

    private static Expense getExpense(Transaction transaction) {
        Expense expense = new Expense();
        expense.setUserId(transaction.getUserId());
        expense.setAmount(transaction.getAmount());
        expense.setCurrencyCode(transaction.getCurrencyCode());
        expense.setCategory(transaction.getCategory());
        expense.setDate(transaction.getDate());
        expense.setDescription(transaction.getDescription());
        expense.setTags(transaction.getTags());
        expense.setRecurrencePattern(transaction.getRecurrencePattern());
        expense.setRecurring(transaction.isIsRecurring());
        expense.setEndDate(transaction.getRecurrenceEndDate());
        return expense;
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
        return transactionRepository.findByUserIdAndTagsIn(userId, Collections.singleton(tags));
    }

    @Override
    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public boolean isOwner(String transactionId, String userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        return transaction.getUserId().equals(userId); // Check if the user owns the transaction
    }

    @Override
    public List<Transaction> getTransactionsByUserInPreferredCurrency(String userId, String preferredCurrency) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        // Convert each transaction's amount to the preferred currency
        return transactions.stream()
                .map(transaction -> convertTransactionToPreferredCurrency(transaction, preferredCurrency))
                .collect(Collectors.toList());
    }

    @Override
    public Transaction convertTransactionToPreferredCurrency(Transaction transaction, String preferredCurrency) {
        String originalCurrency = transaction.getCurrencyCode();
        double originalAmount = transaction.getAmount();

        // Convert the amount to the preferred currency using CurrencyConverter
        double convertedAmount = currencyConverter.convertCurrency(originalCurrency, preferredCurrency, originalAmount,  "LKR");

        // Create a new transaction object with the converted amount and preferred currency
        return getTransaction(transaction, preferredCurrency, convertedAmount);
    }

    private static Transaction getTransaction(Transaction transaction, String preferredCurrency, double convertedAmount) {
        Transaction convertedTransaction = new Transaction();
        convertedTransaction.setId(transaction.getId());
        convertedTransaction.setUserId(transaction.getUserId());
        convertedTransaction.setAmount(convertedAmount);
        convertedTransaction.setCurrencyCode(preferredCurrency);
        convertedTransaction.setCategory(transaction.getCategory());
        convertedTransaction.setTags(transaction.getTags());
        convertedTransaction.setDate(transaction.getDate());
        convertedTransaction.setDescription(transaction.getDescription());
        return convertedTransaction;
    }
}