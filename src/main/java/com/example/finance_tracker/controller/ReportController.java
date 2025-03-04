package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Report;
import com.example.finance_tracker.service.ReportService;
import com.example.finance_tracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }




    @GetMapping("/spending-trend/{userId}")
    public ResponseEntity<Report> generateSpendingTrendReport(
            @PathVariable String userId,
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        Report report = reportService.generateSpendingTrendReport(userId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/income-vs-expense/{userId}")
    public ResponseEntity<Report> generateIncomeVsExpenseReport(
            @PathVariable String userId,
            @RequestParam Date startDate,
            @RequestParam Date endDate) {
        Report report = reportService.generateIncomeVsExpenseReport(userId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/category-wise/{userId}")
    public ResponseEntity<Report> generateCategoryWiseReport(
            @PathVariable String userId,
            @RequestParam String category) {
        Report report = reportService.generateCategoryWiseReport(userId, category);
        return ResponseEntity.ok(report);
    }
}