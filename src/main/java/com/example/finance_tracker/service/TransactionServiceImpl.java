package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Transaction;
import com.example.finance_tracker.model.Income;
import com.example.finance_tracker.model.Expense;
import com.example.finance_tracker.repository.TransactionRepository;
import com.example.finance_tracker.util.CurrencyUtil;
import com.example.finance_tracker.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("transactionService")
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final CurrencyConverter currencyConverter;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final CurrencyUtil currencyUtil;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, CurrencyConverterImpl currencyConverterImpl, CurrencyConverter currencyConverter,
                                  IncomeService incomeService, ExpenseService expenseService, CurrencyUtil currencyUtil) {
        this.transactionRepository = transactionRepository;
        this.currencyConverter = currencyConverter;
        this.incomeService = incomeService;
        this.expenseService = expenseService;
        this.currencyUtil = currencyUtil;
    }

    @Override
    public Transaction addTransaction(Transaction transaction) {
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(transaction.getUserId());
        transaction.setCurrencyCode(baseCurrency);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Automatically create an income or expense record based on the transaction type
        if ("Income".equalsIgnoreCase(transaction.getType())) {
            Income income = new Income();
            income.setUserId(transaction.getUserId());
            income.setAmount(transaction.getAmount());
            income.setCurrencyCode(baseCurrency);
            income.setSource(transaction.getSource());
            income.setDate(transaction.getDate());
            incomeService.addIncome(income);
        } else if ("Expense".equalsIgnoreCase(transaction.getType())) {
            Expense expense = getExpense(transaction);
            expense.setCurrencyCode(baseCurrency);

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
        expense.setCategory(transaction.getCategory());
        expense.setDate(transaction.getDate());
        expense.setDescription(transaction.getDescription());
        expense.setTags(transaction.getTags());
        expense.setRecurrencePattern(transaction.getRecurrencePattern());
        expense.setRecurring(transaction.isIsRecurring());
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
        return transactionRepository.findByUserIdAndTagsIn(userId, tags);
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
        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(userId);

        // Fetch all transactions for the user
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        // Convert each transaction to the preferred currency
        List<Transaction> convertedTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            double convertedAmount = currencyConverter.convertCurrency(
                    transaction.getCurrencyCode(),
                    preferredCurrency,
                    transaction.getAmount(),
                    baseCurrency
            );

            // Log the conversion details
            logger.info("Converting {} {} to {} using base currency {}", transaction.getAmount(), transaction.getCurrencyCode(), preferredCurrency, baseCurrency);
            logger.info("Converted Amount: {}", convertedAmount);

            // Create a new transaction object with the converted amount and preferred currency
            Transaction convertedTransaction = getTransaction(transaction, preferredCurrency, convertedAmount);
            convertedTransactions.add(convertedTransaction);
        }

        return convertedTransactions;
    }

    @Override
    public Transaction convertTransactionToPreferredCurrency(Transaction transaction, String preferredCurrency) {
        String originalCurrency = transaction.getCurrencyCode();
        double originalAmount = transaction.getAmount();

        // Fetch the user's base currency
        String baseCurrency = currencyUtil.getBaseCurrencyForUser(transaction.getUserId());

        // Convert the amount to the preferred currency using CurrencyConverter
        double convertedAmount = currencyConverter.convertCurrency(originalCurrency, preferredCurrency, originalAmount, baseCurrency);

        // Log the conversion details
        logger.info("Converting {} {} to {} using base currency {}", originalAmount, originalCurrency, preferredCurrency, baseCurrency);
        logger.info("Converted Amount: {}", convertedAmount);

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